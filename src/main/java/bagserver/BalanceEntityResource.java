package bagserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import nebula.data.DataHelper;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.impl.EditableEntity;
import nebula.http.Application;
import nebula.http.resource.AbstractResouce;

public class BalanceEntityResource extends AbstractResouce {
	final DataStore<Entity> paymentStore;

	Application app;
	Long key;
	private final DataHelper<Entity, Reader, Writer> json;

	public BalanceEntityResource(Application app, String key) {
		super("text/json", 0, 0);
		this.app = app;
		this.key = Long.parseLong(key);
		this.paymentStore = this.app.getStore("Payment");
		this.json = this.app.getJson(this.app.getType("Balance"));
	}

	BigDecimal summaryPersonal(DataStore<Entity> paymentStore, Long userID) {
		BigDecimal sumFrom = new BigDecimal(0);
		BigDecimal sumTo = new BigDecimal(0);
		for (Entity payment : this.paymentStore.listAll()) {
			Long fromUserID = payment.get("FromUserID");
			Long toUserID = payment.get("ToUserID");
			Long fromAccountType = payment.get("FromAccountTypeID");
			Long toAccountType = payment.get("ToAccountTypeID");
			BigDecimal amount = payment.get("Amount");
			if (userID.equals(fromUserID) && fromAccountType.equals(1L)) {
				sumFrom.add(amount);
			} else if (userID.equals(toUserID) && toAccountType.equals(1L)) {
				sumTo.add(amount);
			}
		}
		return sumTo.min(sumFrom);
	}

	static BigDecimal summaryDepositl(DataStore<Entity> paymentStore, Long userID) {
		BigDecimal sumFrom = new BigDecimal(0);
		BigDecimal sumTo = new BigDecimal(0);
		for (Entity payment : paymentStore.listAll()) {
			Long fromUserID = payment.get("FromUserID");
			Long toUserID = payment.get("ToUserID");
			Long fromAccountType = payment.get("FromAccountTypeID");
			Long toAccountType = payment.get("ToAccountTypeID");
			BigDecimal amount = payment.get("Amount");
			if (userID.equals(fromUserID) && fromAccountType.equals(3L)) {
				sumFrom.add(amount);
			} else if (userID.equals(toUserID) && toAccountType.equals(3L)) {
				sumTo.add(amount);
			}
		}
		return sumTo.min(sumFrom);
	}

	@Override
	protected byte[] get(HttpServletRequest req) throws IOException {
		EditableEntity data = new EditableEntity();
		data.put("ID", key);
		BigDecimal amout = summaryPersonal(paymentStore, key);
		data.put("Amount", amout);

		ByteArrayOutputStream bout = null;
		try {
			bout = new ByteArrayOutputStream();
			Writer write = new OutputStreamWriter(bout, "utf-8");
			json.stringifyTo(data, write);
			write.flush();
			this.lastModified = System.currentTimeMillis();
			byte[] cache = bout.toByteArray();
			this.cache = cache;
			return cache;
		} finally {
			try {
				if (bout != null) bout.close();
			} catch (Exception e) {
			}
		}
	}
}
