package bagserver;

import http.resource.EntityListResouce;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import nebula.data.Application;
import nebula.data.DataSession;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.TransactionCaller;
import nebula.data.impl.EditableEntity;
import nebula.data.json.DataHelper;
import nebula.lang.Type;

import org.joda.time.DateTime;

import util.FileUtil;
import cn.xj.bag.server.manager.UnionpayService;
import cn.xj.bag.server.modal.UpmpNotifyResult;
import cn.xj.bag.server.modal.enums.OrderCurrency;

public class PaymentFlowListEntityResource extends EntityListResouce {

    UnionpayService unionpay = new UnionpayService();
    final DataStore<Entity> stepStore;
    final DataStore<Entity> userStore;
    final DataStore<Entity> paymentStore;
    final PaymentSummary paymentSummary;

    public PaymentFlowListEntityResource(Application app, Type type) {
        super(app, type);
        stepStore = app.getStore("PaymentFlow");
        userStore = app.getStore("User");
        paymentStore = app.getStore("Payment");
        paymentSummary = new PaymentSummary(paymentStore);
    }

    final void userPayDone(DataSession session, EditableEntity fromUser) {
        fromUser.put("BePurchaser", true);
        session.add(userStore, fromUser);
    }

    final void orderPayDone(DataSession session, EditableEntity fromUser, EditableEntity payment) {
        EditableEntity stepOrderItemFlowConfirmed = new EditableEntity();
        stepOrderItemFlowConfirmed.extend(payment);
        stepOrderItemFlowConfirmed.put("PaymentID", payment.get("PaymentID"));
        stepOrderItemFlowConfirmed.put("OrderItemID", payment.get("OrderItemID"));
        stepOrderItemFlowConfirmed.put("StatusID", 2L);
        stepOrderItemFlowConfirmed.put("ActionID", 11L);
        session.add(app.getStore("OrderItemFlow"), stepOrderItemFlowConfirmed);
    }

    @Override
    protected String post(HttpServletRequest req) throws IOException {
        InputStream in = req.getInputStream();
        if (log.isTraceEnabled()) {
            in = FileUtil.print(in);
        }
        final Entity stepIn = jsonHolder.readFrom(new EditableEntity(), new InputStreamReader(in, "utf-8"));

        final StringBuilder sb = new StringBuilder();
        try {
            stepStore.save(new TransactionCaller() {
                @Override
                public void exec(DataSession session) throws Exception {
                    Entity payment = stepIn;

                    Long actionID = payment.get("ActionID");
                    Long fromUserID = payment.get("FromUserID");

                    Long payType = payment.get("PayTypeID");
                    Long payMethod = payment.get("PayMethodID");

                    String description = payment.get("Description");
                    String orderNo = payment.get("OrderNo");
                    String tradeNo = null;
                    DateTime tradeDatetime = new DateTime();
                    BigDecimal amount = payment.get("Amount");

                    long fromAccountType = 1;
                    long toAccountType = 1;
//                    
//                    PayMethodName:"银联",//Bank
//                    FromAccountTypeID:2,//银行
//                    ToAccountTypeID:3,//保证金

                    EditableEntity fromUser = (EditableEntity) userStore.get(fromUserID).editable();

                    switch (actionID.intValue()) {
                    case 1:// "开始支付"
                    {

                        switch (payType.intValue()) {
                        case 1:// "买手保证金"
                            fromAccountType = payMethod;
                            toAccountType = 3;// 保证金

                            switch (payMethod.intValue()) {
                            case 1:
                                if (paymentSummary.summaryDepositl(fromUserID).compareTo(amount) >= 0) {
                                    payment.put("ActionID", 3L);
                                    userPayDone(session, fromUser);
                                } else {
                                    throw new RuntimeException("");
                                }
                                break;
                            case 2:
                                tradeNo = unionpay.trade(tradeDatetime.toDate(), orderNo, OrderCurrency.RMB, amount, description).getTn();
                                payment.put("ActionID", 1L);
                                break;
                            }

                            break;
                        case 2:// "购买保证金"
                            payment.put("OrderItemID", payment.get("OrderItemID"));
                            fromAccountType = payMethod;
                            toAccountType = 3;// 保证金

                            switch (payMethod.intValue()) {
                            case 1:
                                if (paymentSummary.summaryDepositl(fromUserID).compareTo(amount) >= 0) {
                                    payment.put("ActionID", 3L);
                                    orderPayDone(session, fromUser, fromUser);
                                } else {
                                    throw new RuntimeException("");
                                }
                                break;
                            case 2:
                                tradeNo = unionpay.trade(tradeDatetime.toDate(), orderNo, OrderCurrency.RMB, amount, description).getTn();
                                payment.put("ActionID", 1L);
                                break;
                            }

                            break;
                        case 3:// "提现"
                            payMethod = 1L;// 个人账户
                            fromAccountType = 1;// 个人账户
                            toAccountType = 2;// 银行

                            payment.put("ActionID", 2L);

                            break;
                        }

//                        !ID;
//                        @Authorize("Read") From-User;
//                        @Authorize("Read") To-User;
//                        PayType;
                        payment.put("PayTypeName", ((Entity) app.getStore("PayType").get(payType)).get("Name"));
//                        PayMethod;
                        payment.put("PayMethodID", payMethod);
                        payment.put("PayMethodName", ((Entity) app.getStore("PayMethod").get(payMethod)).get("Name"));
//                        From-AccountType;
                        payment.put("FromAccountTypeID", fromAccountType);
                        payment.put("FromAccountTypeName", ((Entity) app.getStore("FromAccountType").get(fromAccountType)).get("Name"));
//                        To-AccountType;
                        payment.put("ToAccountTypeID", toAccountType);
                        payment.put("ToAccountTypeName", ((Entity) app.getStore("FromAccountType").get(toAccountType)).get("Name"));
//                        Datetime;// 交易开始日期时间
                        payment.put("Datatime", tradeDatetime);
//                        Order-No;// 商户订单号
                        payment.put("OrderNo", orderNo);
//                        Amount;        // 清算金额
                        payment.put("Amount", amount);
//                        Description;
                        payment.put("Description", description);
//                        Trade-No; // 交易流水号
                        payment.put("TradeNo", tradeNo);

                        EditableEntity realPayment = new EditableEntity();
                        realPayment.extend(payment);
                        session.add(paymentStore, realPayment);
                        session.flush();
                        payment.put("PaymentID", realPayment.get("ID"));
                        session.add(stepStore, payment);
                        session.flush();
                    }

                        break;
                    case 2:// "支付完成"

                        Long paymentID = payment.get("PaymentID");

                        EditableEntity realPayment = (EditableEntity) (paymentStore.get(paymentID)).editable();

                        Date datetime = ((DateTime) realPayment.get("Datetime")).toDate();
//						BigDecimal amount = payment.get("Amount");

                        UpmpNotifyResult result = null;
                        try {
                            result = unionpay.query(datetime, orderNo);
                            if ("00".equals(result.getTransStatus())) {
                                if (log.isDebugEnabled()) {
                                    log.debug(result);
                                }
//                              Preconditions.checkArgument(datetime.equals(result.getOrderTime()));
//                              Preconditions.checkArgument(orderNo.equals(result.getOrderNumber()));
//                              Preconditions.checkArgument(amount.equals(result.getSettleAmount()));

                                realPayment.put("SettleMonth", result.getSettleDate());
                                session.add(stepStore, payment);
                                session.add(paymentStore, realPayment);

                                {
                                    EditableEntity stepPayConfirmed = new EditableEntity();
                                    stepPayConfirmed.extend(payment);
                                    stepPayConfirmed.put("ActionID", 3L);

                                    switch (payType.intValue()) {
                                    case 1:// "买手保证金"
                                        userPayDone(session, fromUser);
                                        break;
                                    case 2:// "购买保证金"
                                        orderPayDone(session, fromUser, realPayment);
                                        break;
                                    }
                                    session.add(stepStore, stepPayConfirmed);
                                }
                            } else {
                                throw new RuntimeException("");
                            }
                        } catch (Exception e) { // TODO For Test need delete
                            log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");
                            realPayment.put("SettleMonth", "201410");
                            
                            session.add(stepStore, payment);
                            session.add(paymentStore, realPayment);

                            {
                                EditableEntity stepPayConfirmed = new EditableEntity();
                                stepPayConfirmed.extend(payment);
                                stepPayConfirmed.put("ActionID", 3L);

                                switch (payType.intValue()) {
                                case 1:// "买手保证金"
                                    userPayDone(session, fromUser);
                                    break;
                                case 2:// "购买保证金"
                                    orderPayDone(session, fromUser, realPayment);
                                    break;
                                }
                                session.add(stepStore, stepPayConfirmed);
                            }
                        }
                        break;
                    }

                    StringWriter sw = new StringWriter();
                    DataHelper<Entity, Reader, Writer> json = app.getJson(app.getType("UserPayFlow"));
                    json.stringifyTo(payment, sw);

                    sb.append(sw.toString());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
