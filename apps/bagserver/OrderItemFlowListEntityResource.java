package bagserver;

import http.resource.EntityListResouce;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nebula.data.Application;
import nebula.data.DataSession;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.TransactionCaller;
import nebula.data.impl.EditableEntity;
import nebula.lang.Type;

import org.joda.time.DateTime;

import util.FileUtil;

public class OrderItemFlowListEntityResource extends EntityListResouce {

	public OrderItemFlowListEntityResource(Application app, Type type) {
		super(app, type);
	}

	@Override
	protected String post(HttpServletRequest req) throws IOException {
		final DataStore<Entity> stepStore = datastoreHolder;
		final DataStore<Entity> itemStore = app.getStore("OrderItem");
		final DataStore<Entity> bidStore = app.getStore("Bid");
		final DataStore<Entity> productStore = app.getStore("Product");

		InputStream in = req.getInputStream();
		if (log.isTraceEnabled()) {
			in = FileUtil.print(in);
		}
		final Entity step = jsonHolder.readFrom(null, new InputStreamReader(in, "utf-8"));

		try {
			stepStore.save(new TransactionCaller() {
				@Override
				public void exec(DataSession session) throws Exception {
					Long actionID = step.get("ActionID");

					EditableEntity exts = (EditableEntity) step.getEntity("Extends");
					if (exts == null) {
						exts = new EditableEntity();
						step.put("Extends", exts);
					}

					Long itemID = step.get("OrderItemID");
					EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

					switch (actionID.intValue()) {
					case 2:// "选中买手"
						exts.put("BidSucceedDatetime", new DateTime());
						List<Entity> bids = bidStore.getClassificator("OrderItem").getData(String.valueOf(itemID));
						Long succeedID = step.getEntity("Bid").get("ID");
						for (Entity entity : bids) {
							EditableEntity editableEntity = (EditableEntity) entity.editable();
							Long bidID = editableEntity.get("ID");
							if (bidID.equals(succeedID)) {
								editableEntity.put("StatusID", 2L);
								editableEntity.put("StatusName", "成功");
							} else {
								editableEntity.put("StatusID", 3L);
								editableEntity.put("StatusName", "失败");
							}
							session.add(bidStore, editableEntity);
						}
						break;
					case 3:// "开始购买"
						exts.put("PurchasingStartDatetime", new DateTime());
						break;
					case 4:// "购买完成"
						exts.put("PurchasingEndDatetime", new DateTime());
						break;
					case 5:// "开始发货"
						exts.put("DeliveryStartDatetime", new DateTime());
						break;
					case 6:// "确认收货"
						exts.put("DeliveryArriveDatetime", new DateTime());
						EditableEntity product = new EditableEntity();
						Entity itemProduct = (EditableEntity) item.getEntity("Product");
						product.extend(itemProduct);
						product.put("OrderItemID", itemID);
						session.add(productStore, product);
						break;
					case 7:// "放弃购买"
						exts.put("CancelPurchasingDatetime", new DateTime());
						break;
					case 8:// "放弃订单"
						exts.put("CancelOrderDatetime", new DateTime());
						break;
					}
					session.add(stepStore, step);
					item.put("StatusID", step.get("StatusID"));
					item.put("StatusName", step.get("StatusName"));
					item.put("ActionID", step.get("ActionID"));
					item.put("ActionName", step.get("ActionName"));
					session.add(itemStore, item);
					session.flush();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return req.getPathInfo() + step.getID();
	}

}
