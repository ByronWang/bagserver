package bagserver;

import http.resource.EntityListResouce;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;

import util.FileUtil;

import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.impl.EditableEntity;
import nebula.data.json.DataHelper;
import nebula.lang.Type;

public class OrderItemFlowListEntityResource extends EntityListResouce {

	public OrderItemFlowListEntityResource(Type typeBroker, DataHelper<Entity, Reader, Writer> jsonSimpleHolder, DataHelper<Entity, Reader, Writer> jsonHolder,
			DataStore<Entity> datas) {
		super(typeBroker, jsonSimpleHolder, jsonHolder, datas);
	}

	@Override
	protected String post(HttpServletRequest req) throws IOException {
		DataStore<Entity> store = datastoreHolder;
		InputStream in = req.getInputStream();
		if (log.isTraceEnabled()) {
			in = FileUtil.print(in);
		}
		Entity inData = jsonHolder.readFrom(null, new InputStreamReader(in, "utf-8"));
		
		long actionID = inData.get("ActionID");
		
		EditableEntity exts = (EditableEntity)inData.getEntity("Extends");
		
		switch((int)actionID){
		case 1://"选中买手"
			exts.put("BidSucceedDatetime", new DateTime());
			break;
		case 2://"放弃订单"
			exts.put("CancelOrderDatetime", new DateTime());
			break;
		case 3:// "开始购买"
			exts.put("PurchasingStartDatetime", new DateTime());
			break;
		case 4://"购买完成"
			exts.put("PurchasingEndDatetime", new DateTime());
			break;
		case 5://"开始发货"
			exts.put("DeliveryStartDatetime", new DateTime());
			break;
		case 6://"确认收货"
			exts.put("DeliveryArriveDatetime", new DateTime());
			break;
		case 7://"放弃购买"
			exts.put("CancelPurchasingDatetime", new DateTime());
			break;
		}

		store.add(inData);
		store.flush();
		return req.getPathInfo() + inData.getID();
	}

}
