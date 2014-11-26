package bagserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import nebula.data.DataSession;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.TransactionCaller;
import nebula.data.impl.EditableEntity;
import nebula.http.Application;
import nebula.http.resource.rest.EntityListResouce;

import org.joda.time.DateTime;

import util.FileUtil;
import cn.xj.bag.server.manager.UnionpayService;
import cn.xj.bag.server.modal.UpmpNotifyResult;
import cn.xj.bag.server.modal.enums.OrderCurrency;

public class PaymentFlowListEntityResource extends EntityListResouce {

    UnionpayService unionpay = new UnionpayService();
    final DataStore<Entity> stepStore;
    final DataStore<Entity> userStore;
    final DataStore<Entity> statusStore;
    final DataStore<Entity> actionStore;
    final DataStore<Entity> paymentStore;
    final PaymentSummary paymentSummary;

    public PaymentFlowListEntityResource(Application app) {
        super(app,app.getType("PaymentFlow"));
        stepStore = app.getStore("PaymentFlow");
        userStore = app.getStore("User");
        paymentStore = app.getStore("Payment");
        statusStore = app.getStore("OrderStatus");
        actionStore = app.getStore("Action");
        paymentSummary = new PaymentSummary(paymentStore);
    }

    final void userPayDone(DataSession session, EditableEntity fromUser) {
        fromUser.put("BePurchaser", true);
        session.add(userStore, fromUser);
    }

    final void orderPayDone(DataSession session, EditableEntity fromUser, EditableEntity payment) {
        Long itemID = Long.parseLong((String) payment.get("OrderNo"));
        EditableEntity stepOrderItemFlowConfirmed = new EditableEntity();
        stepOrderItemFlowConfirmed.extend(payment);
        stepOrderItemFlowConfirmed.put("PaymentID", payment.get("PaymentID"));
        stepOrderItemFlowConfirmed.put("OrderItemID", itemID);
        stepOrderItemFlowConfirmed.put("StatusID", 2L);
        stepOrderItemFlowConfirmed.put("StatusName", statusStore.get(2L).get("Name"));
        stepOrderItemFlowConfirmed.put("ActionID", 11L);
        stepOrderItemFlowConfirmed.put("ActionName", actionStore.get(11L).get("Name"));
        session.add(app.getStore("OrderItemFlow"), stepOrderItemFlowConfirmed);

        payment.put("SettleMonth", "201410");
        session.add(stepStore, payment);
        DataStore<Entity> itemStore = app.getStore("OrderItem");
        EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

        item.put("StatusID", stepOrderItemFlowConfirmed.get("StatusID"));
        item.put("StatusName", stepOrderItemFlowConfirmed.get("StatusName"));
        item.put("ActionID", stepOrderItemFlowConfirmed.get("ActionID"));
        item.put("ActionName", stepOrderItemFlowConfirmed.get("ActionName"));
        session.add(itemStore, item);
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
                    Entity paymentFlowStep = stepIn;

                    Long actionID = paymentFlowStep.get("ActionID");
                    Long fromUserID = paymentFlowStep.get("FromUserID");

                    Long payType = paymentFlowStep.get("PayTypeID");
                    Long payMethod = paymentFlowStep.get("PayMethodID");

                    String description = paymentFlowStep.get("Description");
                    String orderNo = paymentFlowStep.get("OrderNo");
                    String realOrderNo = orderNo + String.valueOf(System.currentTimeMillis());
                    paymentFlowStep.put("realOrderNo", realOrderNo);

                    String tradeNo = null;
                    DateTime tradeDatetime = new DateTime();
                    BigDecimal amount = paymentFlowStep.get("Amount");

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
                                    paymentFlowStep.put("ActionID", 3L);
                                    paymentFlowStep.put("ActionName", actionStore.get(3L).get("Name"));
                                    userPayDone(session, fromUser);
                                } else {
                                    throw new RuntimeException("");
                                }
                                break;
                            case 2:
                                tradeNo = unionpay.trade(tradeDatetime.toDate(), realOrderNo, OrderCurrency.RMB, amount, description).getTn();
                                paymentFlowStep.put("ActionID", 1L);
                                paymentFlowStep.put("ActionName", actionStore.get(1L).get("Name"));
                                break;
                            }

                            break;
                        case 2:// "购买保证金"
                            paymentFlowStep.put("OrderItemID", paymentFlowStep.get("OrderItemID"));
                            fromAccountType = payMethod;
                            toAccountType = 3;// 保证金

                            switch (payMethod.intValue()) {
                            case 1:
                                if (paymentSummary.summaryDepositl(fromUserID).compareTo(amount) >= 0) {
                                    paymentFlowStep.put("ActionID", 3L);
                                    paymentFlowStep.put("ActionName", actionStore.get(3L).get("Name"));
                                    orderPayDone(session, fromUser,(EditableEntity)paymentFlowStep);
                                } else {
                                    throw new RuntimeException("");
                                }
                                break;
                            case 2:
                                tradeNo = unionpay.trade(tradeDatetime.toDate(), realOrderNo, OrderCurrency.RMB, amount, description).getTn();
                                paymentFlowStep.put("ActionID", 1L);
                                paymentFlowStep.put("ActionName", actionStore.get(1L).get("Name"));
                                break;
                            }

                            break;
                        case 3:// "提现"
                            payMethod = 1L;// 个人账户
                            fromAccountType = 1;// 个人账户
                            toAccountType = 2;// 银行

                            paymentFlowStep.put("ActionID", 2L);
                            paymentFlowStep.put("ActionName", actionStore.get(2L).get("Name"));

                            break;
                        }

//                        !ID;
//                        @Authorize("Read") From-User;
//                        @Authorize("Read") To-User;
//                        PayType;
                        paymentFlowStep.put("PayTypeName", ((Entity) app.getStore("PayType").get(payType)).get("Name"));
//                        PayMethod;
                        paymentFlowStep.put("PayMethodID", payMethod);
                        paymentFlowStep.put("PayMethodName", ((Entity) app.getStore("PayMethod").get(payMethod)).get("Name"));
//                        From-AccountType;
                        paymentFlowStep.put("FromAccountTypeID", fromAccountType);
                        paymentFlowStep.put("FromAccountTypeName", ((Entity) app.getStore("AccountType").get(fromAccountType)).get("Name"));
//                        To-AccountType;
                        paymentFlowStep.put("ToAccountTypeID", toAccountType);
                        paymentFlowStep.put("ToAccountTypeName", ((Entity) app.getStore("AccountType").get(toAccountType)).get("Name"));
//                        Datetime;// 交易开始日期时间
                        paymentFlowStep.put("Datetime", tradeDatetime);
//                        Order-No;// 商户订单号
                        paymentFlowStep.put("OrderNo", orderNo);
                        paymentFlowStep.put("RealOrderNo", orderNo);
//                        Amount;        // 清算金额
                        paymentFlowStep.put("Amount", amount);
//                        Description;
                        paymentFlowStep.put("Description", description);
//                        Trade-No; // 交易流水号
                        paymentFlowStep.put("TradeNo", tradeNo);

                        EditableEntity realPayment = new EditableEntity();
                        realPayment.extend(paymentFlowStep);
                        session.add(paymentStore, realPayment);
                        session.flush();
                        paymentFlowStep.put("PaymentID", realPayment.get("ID"));
                        session.add(stepStore, paymentFlowStep);
                        session.flush();
                    }

                        break;
                    case 2:// "支付完成"

                        Long paymentID = paymentFlowStep.get("PaymentID");

                        EditableEntity realPayment = (EditableEntity) (paymentStore.get(paymentID)).editable();

                        Date datetime = ((DateTime) realPayment.get("Datetime")).toDate();
//						BigDecimal amount = payment.get("Amount");

                        UpmpNotifyResult result = null;
                        try {
                            result = unionpay.query(datetime, realOrderNo);
                            if ("00".equals(result.getTransStatus())) {
                                if (log.isDebugEnabled()) {
                                    log.debug(result);
                                }
//                              Preconditions.checkArgument(datetime.equals(result.getOrderTime()));
//                              Preconditions.checkArgument(orderNo.equals(result.getOrderNumber()));
//                              Preconditions.checkArgument(amount.equals(result.getSettleAmount()));

                                realPayment.put("SettleMonth", result.getSettleDate());
                                session.add(stepStore, paymentFlowStep);
                                session.add(paymentStore, realPayment);

                                {
                                    EditableEntity stepPayConfirmed = new EditableEntity();
                                    stepPayConfirmed.extend(paymentFlowStep);
                                    stepPayConfirmed.put("ActionID", 3L);
                                    stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));

                                    switch (payType.intValue()) {
                                    case 1:// "买手保证金"
                                        userPayDone(session, fromUser);
                                        break;
                                    case 2:// "购买保证金"
                                        orderPayDone(session, fromUser, realPayment);
                                        break;
                                    }
                                    session.add(stepStore, stepPayConfirmed);
                                    paymentFlowStep = stepPayConfirmed;
                                }
                            } else {
                                throw new RuntimeException("");
                            }
                        } catch (Exception e) { // TODO For Test need delete
                            log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");
                            realPayment.put("SettleMonth", "201410");

                            session.add(stepStore, paymentFlowStep);
                            session.add(paymentStore, realPayment);

                            {
                                EditableEntity stepPayConfirmed = new EditableEntity();
                                stepPayConfirmed.extend(paymentFlowStep);
                                stepPayConfirmed.put("ActionID", 3L);
                                stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));

                                switch (payType.intValue()) {
                                case 1:// "买手保证金"
                                    userPayDone(session, fromUser);
                                    break;
                                case 2:// "购买保证金"
                                    orderPayDone(session, fromUser, stepPayConfirmed);
                                    break;
                                }
                                session.add(stepStore, stepPayConfirmed);
                                paymentFlowStep = stepPayConfirmed;
                            }
                        }
                        session.flush();
                        break;
                    }

                    StringWriter sw = new StringWriter();
                    jsonHolder.stringifyTo(paymentFlowStep, sw);

                    sb.append(sw.toString());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
