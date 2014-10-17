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

public class UserPayFlowListEntityResource extends EntityListResouce {

    UnionpayService unionpay = new UnionpayService();
    final DataStore<Entity> stepStore;
    final DataStore<Entity> userStore;
    final DataStore<Entity> paymentStore;

    public UserPayFlowListEntityResource(Application app, Type type) {
        super(app, type);
        stepStore = app.getStore("UserFlow");
        userStore = app.getStore("User");
        paymentStore = app.getStore("Payment");
    }

    @Override
    protected String post(HttpServletRequest req) throws IOException {
        InputStream in = req.getInputStream();
        if (log.isTraceEnabled()) {
            in = FileUtil.print(in);
        }
        final Entity stepIn = jsonHolder.readFrom(new EditableEntity(), new InputStreamReader(in, "utf-8"));

        final StringBuilder sb =new StringBuilder();
        try {
            stepStore.save(new TransactionCaller() {
                @Override
                public void exec(DataSession session) throws Exception {
                    Entity step = stepIn;
                    
                    Long actionID = step.get("ActionID");
                    Long userID = step.get("UserID");

                    EditableEntity user = (EditableEntity) userStore.get(userID).editable();

                    switch (actionID.intValue()) {
                    case 1:// "开始支付"
                    {
                        BigDecimal amount = new BigDecimal(app.getProperty("PayToBecomePurchaser.price", "100"));
                        String orderNo = String.valueOf((Long) user.get("ID")) + String.valueOf(System.currentTimeMillis());
                        DateTime tradeDatetime = new DateTime();

                        String description = app.getProperty("PayToBecomePurchaser.comment", "成为买家保证金");

                        EditableEntity payment = new EditableEntity();
                        payment.put("Datetime", tradeDatetime);
                        payment.put("OrderNo", orderNo);
                        payment.put("Amount", amount);
                        payment.put("Desccription", description);

                        String tradeNo = unionpay.trade(tradeDatetime.toDate(), orderNo, OrderCurrency.RMB, amount, description).getTn();

                        payment.put("TradeNo", tradeNo);

                        session.add(paymentStore, payment);
                        session.flush();

                        step.put("PaymentID", payment.get("ID"));
                        step.put("TradeNo", payment.get("TradeNo"));
                        step.put("ActionID", 1L);

                        session.add(stepStore, step);
                    }

                        break;
                    case 2:// "支付完成"

                        Long paymentID = step.get("PaymentID");

                        EditableEntity payment = (EditableEntity) (paymentStore.get(paymentID)).editable();

                        Date datetime = ((DateTime) payment.get("Datetime")).toDate();
                        String orderNo = payment.get("OrderNo");
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

                                payment.put("SettleMonth", result.getSettleDate());
                                session.add(stepStore, payment);

                                EditableEntity stepPayConfirmed = new EditableEntity();
                                stepPayConfirmed.extend(step);
                                stepPayConfirmed.put("PaymentID", payment.get("ID"));
                                stepPayConfirmed.put("ActionID", 3L);
                                user.put("BePurchaser", true);

                                session.add(stepStore, stepPayConfirmed);
                                step=stepPayConfirmed;
                            } else {
                                throw new RuntimeException("");
                            }
                        } catch (Exception e) { // TODO For Test need delete
                            log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");
                            payment.put("SettleMonth", "201410");
                            session.add(stepStore, payment);

                            EditableEntity stepPayConfirmed = new EditableEntity();
                            stepPayConfirmed.extend(step);
                            stepPayConfirmed.put("PaymentID", payment.get("ID"));
                            stepPayConfirmed.put("ActionID", 3L);

                            user.put("BePurchaser", true);
                            session.add(stepStore, stepPayConfirmed);
                            step=stepPayConfirmed;
                        }
                        break;
                    }
                    session.flush();
                    
                    StringWriter sw =  new StringWriter();
                    DataHelper<Entity, Reader, Writer>  json = app.getJson(app.getType("UserPayFlow"));
                    json.stringifyTo(step, sw);
                    
                    sb.append(sw.toString());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
