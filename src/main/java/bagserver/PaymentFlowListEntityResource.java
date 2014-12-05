package bagserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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

import com.google.common.base.Predicate;

public class PaymentFlowListEntityResource extends EntityListResouce {

	UnionpayService unionpay = new UnionpayService();
	final DataStore<Entity> stepStore;
	final DataStore<Entity> userStore;
	final DataStore<Entity> statusStore;
	final DataStore<Entity> actionStore;
	final DataStore<Entity> paymentStore;
	Timer timer;

	final long delay;

	class ConfirmRuner extends TimerTask {

		@Override
		public void run() {
			final Collection<Entity> list = stepStore.filter(new Predicate<Entity>() {
				@Override
				public boolean apply(Entity input) {
					return ((Long) 2L).equals(input.get("TransStatus"));
				}
			});

			final boolean[] allSucceed = new boolean[1];

			try {
				stepStore.save(new TransactionCaller() {
					@Override
					public void exec(DataSession session) throws Exception {
						for (Entity entity : list) {
							boolean result = doConfirm(entity, session);
							allSucceed[0] = allSucceed[0] && result;
						}
						session.flush();
					}
				});

				if (allSucceed[0]) {
					stopTimer();
				}
			} catch (Exception e) {
				log.error(e);
				throw new RuntimeException(e);
			}

		}
	}

	boolean timerRuning = false;

	private void startTimer() {
		if (!timerRuning) {
			timer.schedule(new ConfirmRuner(), delay, 100);
			timerRuning = true;
		}
	}

	private void stopTimer() {
		PaymentFlowListEntityResource.this.timer.cancel();
		timerRuning = false;
	}

	public PaymentFlowListEntityResource(Application app) {
		super(app, app.getType("PaymentFlow"));
		stepStore = app.getStore("PaymentFlow");
		userStore = app.getStore("User");
		paymentStore = app.getStore("Payment");
		statusStore = app.getStore("OrderStatus");
		actionStore = app.getStore("Action");
		this.delay = Long.parseLong(app.getProperty("app.paymentTaskDelay", "3000"));
		timer = new Timer();
	}

	final void userPayDone(DataSession session, EditableEntity fromUser) {
		fromUser.put("BePurchaser", true);
		session.add(userStore, fromUser);
	}

	final boolean doConfirm(Entity stepCurrent, DataSession paymentFlow) {
		String realOrderNo = stepCurrent.get("RealOrderNo");
		Long payType = stepCurrent.get("PayTypeID");
		Long paymentID = stepCurrent.get("PaymentID");
		EditableEntity realPayment = (EditableEntity) (paymentStore.get(paymentID)).editable();

		Date datetime = ((DateTime) realPayment.get("Datetime")).toDate();
		UpmpNotifyResult result = null;
		try {
			result = unionpay.query(datetime, realOrderNo);
			if ("00".equals(result.getTransStatus())) {
				if (log.isDebugEnabled()) {
					log.debug(result);
				}
//              Preconditions.checkArgument(datetime.equals(result.getOrderTime()));
//              Preconditions.checkArgument(orderNo.equals(result.getOrderNumber()));
//              Preconditions.checkArgument(amount.equals(result.getSettleAmount()));

				{
					EditableEntity stepPayConfirmed = new EditableEntity();
					stepPayConfirmed.extend(stepCurrent);
					stepPayConfirmed.put("ActionID", 3L);
					stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));

					realPayment.put("SettleMonth", result.getSettleDate());
					realPayment.put("TransStatus", 3L);

					switch (payType.intValue()) {
					case 1:// "买手保证金"
						Long fromUserID = stepCurrent.get("FromUserID");
						EditableEntity fromUser = (EditableEntity) userStore.get(fromUserID).editable();
						userPayDone(paymentFlow, fromUser);
						break;
					case 2:// "购买保证金"
						orderPayConfirmed(paymentFlow, stepCurrent);
						break;
					}
					paymentFlow.add(stepStore, stepPayConfirmed);
				}
				return true;
			}
		} catch (Exception e) { // TODO For Test need delete
			log.info(e);
			log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");
			startTimer();
//
//			if (!"true".equals(app.getProperty("app.auth", "true"))) {
//
//				EditableEntity stepPayConfirmed = new EditableEntity();
//				stepPayConfirmed.extend(stepCurrent);
//				stepPayConfirmed.put("ActionID", 3L);
//				stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));
//
//				realPayment.put("SettleMonth", "201410");
//				realPayment.put("TransStatus", 3L);
//
//				switch (payType.intValue()) {
//				case 1:// "买手保证金"
//					Long fromUserID = stepCurrent.get("FromUserID");
//					EditableEntity fromUser = (EditableEntity) userStore.get(fromUserID).editable();
//					break;
//				case 2:// "购买保证金"
//					orderPayConfirmed(paymentFlow, realPayment);
//					break;
//				}
//				paymentFlow.add(stepStore, stepPayConfirmed);
//			}

		}
		return false;
	}

	final void orderPayBegin(DataSession session, final Entity paymentFlow) {
		Long itemID = Long.parseLong((String) paymentFlow.get("OrderNo"));
		EditableEntity stepOrderItemFlow = new EditableEntity();
		stepOrderItemFlow.put("PaymentID", paymentFlow.get("PaymentID"));
		stepOrderItemFlow.put("OrderItemID", itemID);
		stepOrderItemFlow.put("StatusID", 1L);
		stepOrderItemFlow.put("StatusName", statusStore.get(1L).get("Name"));
		stepOrderItemFlow.put("ActionID", 9L);
		stepOrderItemFlow.put("ActionName", actionStore.get(9L).get("Name"));
		session.add(app.getStore("OrderItemFlow"), stepOrderItemFlow);
		DataStore<Entity> itemStore = app.getStore("OrderItem");
		EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

		item.put("StatusID", stepOrderItemFlow.get("StatusID"));
		item.put("StatusName", stepOrderItemFlow.get("StatusName"));
		item.put("ActionID", stepOrderItemFlow.get("ActionID"));
		item.put("ActionName", stepOrderItemFlow.get("ActionName"));
		item.put("LastUpdated", new DateTime());
		session.add(itemStore, item);
	}

	final void orderPayFinished(DataSession session, final Entity paymentFlow) {
		Long itemID = Long.parseLong((String) paymentFlow.get("OrderNo"));
		EditableEntity stepOrderItemFlow = new EditableEntity();
		stepOrderItemFlow.put("PaymentID", paymentFlow.get("PaymentID"));
		stepOrderItemFlow.put("OrderItemID", itemID);
		stepOrderItemFlow.put("StatusID", 1L);
		stepOrderItemFlow.put("StatusName", statusStore.get(1L).get("Name"));
		stepOrderItemFlow.put("ActionID", 10L);
		stepOrderItemFlow.put("ActionName", actionStore.get(10L).get("Name"));
		session.add(app.getStore("OrderItemFlow"), stepOrderItemFlow);

		DataStore<Entity> itemStore = app.getStore("OrderItem");
		EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

		item.put("StatusID", stepOrderItemFlow.get("StatusID"));
		item.put("StatusName", stepOrderItemFlow.get("StatusName"));
		item.put("ActionID", stepOrderItemFlow.get("ActionID"));
		item.put("ActionName", stepOrderItemFlow.get("ActionName"));
		item.put("LastUpdated", new DateTime());
		session.add(itemStore, item);
	}

	final void orderPayConfirmed(DataSession session, final Entity paymentFlow) {
		Long itemID = Long.parseLong((String) paymentFlow.get("OrderNo"));
		EditableEntity stepOrderItemFlow = new EditableEntity();
		stepOrderItemFlow.put("PaymentID", paymentFlow.get("PaymentID"));
		stepOrderItemFlow.put("OrderItemID", itemID);
		stepOrderItemFlow.put("StatusID", 2L);
		stepOrderItemFlow.put("StatusName", statusStore.get(2L).get("Name"));
		stepOrderItemFlow.put("ActionID", 11L);
		stepOrderItemFlow.put("ActionName", actionStore.get(11L).get("Name"));
		session.add(app.getStore("OrderItemFlow"), stepOrderItemFlow);

		DataStore<Entity> itemStore = app.getStore("OrderItem");
		EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

		item.put("StatusID", stepOrderItemFlow.get("StatusID"));
		item.put("StatusName", stepOrderItemFlow.get("StatusName"));
		item.put("ActionID", stepOrderItemFlow.get("ActionID"));
		item.put("ActionName", stepOrderItemFlow.get("ActionName"));
		item.put("LastUpdated", new DateTime());
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
					Entity stepCurrent = stepIn;

					Long actionID = stepCurrent.get("ActionID");
					Long fromUserID = stepCurrent.get("FromUserID");

					Long payType = stepCurrent.get("PayTypeID");
					Long payMethod = stepCurrent.get("PayMethodID");

					String description = stepCurrent.get("Description");
					String orderNo = stepCurrent.get("OrderNo");

					String tradeNo = null;
					DateTime tradeDatetime = new DateTime();
					BigDecimal amount = stepCurrent.get("Amount");

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

						String realOrderNo = orderNo + String.valueOf(System.currentTimeMillis());
						stepCurrent.put("RealOrderNo", realOrderNo);

						switch (payType.intValue()) {
						case 1:// "买手保证金"
							fromAccountType = payMethod;
							toAccountType = 3;// 保证金

							switch (payMethod.intValue()) {
							case 1:
								if (BalanceEntityResource.summaryDepositl(paymentStore, fromUserID).compareTo(amount) >= 0) {
									stepCurrent.put("ActionID", 3L);
									stepCurrent.put("ActionName", actionStore.get(3L).get("Name"));
									userPayDone(session, fromUser);
								} else {
									throw new RuntimeException("");
								}
								break;
							case 2:
								tradeNo = unionpay.trade(tradeDatetime.toDate(), realOrderNo, OrderCurrency.RMB, amount, description).getTn();
								stepCurrent.put("ActionID", 1L);
								stepCurrent.put("ActionName", actionStore.get(1L).get("Name"));
								break;
							}

							break;
						case 2:// "购买保证金"
							stepCurrent.put("OrderItemID", stepCurrent.get("OrderItemID"));
							fromAccountType = payMethod;
							toAccountType = 3;// 保证金

							switch (payMethod.intValue()) {
							case 1:
								if (BalanceEntityResource.summaryDepositl(paymentStore, fromUserID).compareTo(amount) >= 0) {
									stepCurrent.put("ActionID", 3L);
									stepCurrent.put("ActionName", actionStore.get(3L).get("Name"));
									orderPayConfirmed(session, (EditableEntity) stepCurrent);
								} else {
									throw new RuntimeException("");
								}
								break;
							case 2:
								tradeNo = unionpay.trade(tradeDatetime.toDate(), realOrderNo, OrderCurrency.RMB, amount, description).getTn();
								stepCurrent.put("ActionID", 1L);
								stepCurrent.put("ActionName", actionStore.get(1L).get("Name"));
								break;
							}

							break;
						case 3:// "提现"
							payMethod = 1L;// 个人账户
							fromAccountType = 1;// 个人账户
							toAccountType = 2;// 银行

							stepCurrent.put("ActionID", 2L);
							stepCurrent.put("ActionName", actionStore.get(2L).get("Name"));

							break;
						}

//                        !ID;
//                        @Authorize("Read") From-User;
//                        @Authorize("Read") To-User;
//                        PayType;
						stepCurrent.put("PayTypeName", ((Entity) app.getStore("PayType").get(payType)).get("Name"));
//                        PayMethod;
						stepCurrent.put("PayMethodID", payMethod);
						stepCurrent.put("PayMethodName", ((Entity) app.getStore("PayMethod").get(payMethod)).get("Name"));
//                        From-AccountType;
						stepCurrent.put("FromAccountTypeID", fromAccountType);
						stepCurrent.put("FromAccountTypeName", ((Entity) app.getStore("AccountType").get(fromAccountType)).get("Name"));
//                        To-AccountType;
						stepCurrent.put("ToAccountTypeID", toAccountType);
						stepCurrent.put("ToAccountTypeName", ((Entity) app.getStore("AccountType").get(toAccountType)).get("Name"));
//                        Datetime;// 交易开始日期时间
						stepCurrent.put("Datetime", tradeDatetime);
//                        Order-No;// 商户订单号
						stepCurrent.put("OrderNo", orderNo);
						stepCurrent.put("RealOrderNo", realOrderNo);
//                        Amount;        // 清算金额
						stepCurrent.put("Amount", amount);
//                        Description;
						stepCurrent.put("Description", description);
//                        Trade-No; // 交易流水号
						stepCurrent.put("TradeNo", tradeNo);

						EditableEntity realPayment = new EditableEntity();
						realPayment.extend(stepCurrent);
						realPayment.put("TransStatus", stepCurrent.get("ActionID"));
						session.add(paymentStore, realPayment);
						session.flush();
						stepCurrent.put("PaymentID", realPayment.get("ID"));
						session.add(stepStore, stepCurrent);

						switch (payType.intValue()) {
						case 2:// "购买保证金"
							orderPayBegin(session, stepCurrent);
							break;
						}
						session.flush();
					}

						break;
					case 2:// "支付完成"

						Long paymentID = stepCurrent.get("PaymentID");

						EditableEntity realPayment = (EditableEntity) (paymentStore.get(paymentID)).editable();

//						BigDecimal amount = payment.get("Amount");
						realPayment.put("TransStatus", 2L);
						switch (payType.intValue()) {
						case 2:// "购买保证金"
							orderPayFinished(session, stepCurrent);
							break;
						}
						session.add(stepStore, stepCurrent);

					case 3:
						paymentID = stepCurrent.get("PaymentID");
						realPayment = (EditableEntity) (paymentStore.get(paymentID)).editable();

						Date datetime = ((DateTime) realPayment.get("Datetime")).toDate();
						UpmpNotifyResult result = null;
						try {
							String realOrderNo = stepCurrent.get("RealOrderNo");
							result = unionpay.query(datetime, realOrderNo);
							if ("00".equals(result.getTransStatus())) {
								if (log.isDebugEnabled()) {
									log.debug(result);
								}
//                              Preconditions.checkArgument(datetime.equals(result.getOrderTime()));
//                              Preconditions.checkArgument(orderNo.equals(result.getOrderNumber()));
//                              Preconditions.checkArgument(amount.equals(result.getSettleAmount()));

								{
									EditableEntity stepPayConfirmed = new EditableEntity();
									stepPayConfirmed.extend(stepCurrent);
									stepPayConfirmed.put("ActionID", 3L);
									stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));

									realPayment.put("SettleMonth", result.getSettleDate());
									realPayment.put("TransStatus", 3L);

									switch (payType.intValue()) {
									case 1:// "买手保证金"
										userPayDone(session, fromUser);
										break;
									case 2:// "购买保证金"
										orderPayConfirmed(session, stepCurrent);
										break;
									}
									session.add(stepStore, stepPayConfirmed);
									stepCurrent = stepPayConfirmed;
								}
							}
						} catch (Exception e) { // TODO For Test need delete
							log.info(e);
							log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");

//							if (!"true".equals(app.getProperty("app.auth", "true"))) {
//
//								EditableEntity stepPayConfirmed = new EditableEntity();
//								stepPayConfirmed.extend(paymentFlowStep);
//								stepPayConfirmed.put("ActionID", 3L);
//								stepPayConfirmed.put("ActionName", actionStore.get(3L).get("Name"));
//
//								realPayment.put("SettleMonth", "201410");
//								realPayment.put("TransStatus", 3L);
//
//								switch (payType.intValue()) {
//								case 1:// "买手保证金"
//									userPayDone(session, fromUser);
//									break;
//								case 2:// "购买保证金"
//									orderPayConfirmed(session, realPayment);
//									break;
//								}
//								session.add(stepStore, stepPayConfirmed);
//								paymentFlowStep = stepPayConfirmed;
//							}

						}
						session.add(stepStore, realPayment);
						session.flush();
						break;
					}

					StringWriter sw = new StringWriter();
					jsonHolder.stringifyTo(stepCurrent, sw);

					sb.append(sw.toString());
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}
}
