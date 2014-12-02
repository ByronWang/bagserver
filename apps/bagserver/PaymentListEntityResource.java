package bagserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nebula.data.DataHelper;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.http.Application;
import nebula.http.resource.AbstractResouce;

import org.eclipse.jetty.util.URIUtil;

public class PaymentListEntityResource extends AbstractResouce {
	final DataStore<Entity> paymentStore;
	private final DataHelper<Entity, Reader, Writer> jsonHolder;

	public PaymentListEntityResource(Application app) {
		super("text/json", 0, 1000);
		this.paymentStore = app.getStore("Payment");
		this.jsonHolder = app.getJson(app.getType("Payment"));
	}

	@Override
	protected byte[] get(HttpServletRequest req) {
		String query = URIUtil.decodePath(req.getQueryString());
		byte[] cache;
		if (query == null || query.length() == 0) {
			return new byte[0];
		} else {
			int page = -1;
			int pagesize = -1;
			StringBuilder sb = new StringBuilder();
			int next = 0;
			int off = 0;
			Long userID = null;
			while ((next = query.indexOf('&', off)) != -1) {
				if (query.startsWith("User=", off)) {
					userID = Long.parseLong(query.substring(off + 5, next));
				} else if (query.startsWith("page=", off)) {
					page = Integer.parseInt(query.substring(off + 5, next));
				} else if (query.startsWith("pagesize=", off)) {
					pagesize = Integer.parseInt(query.substring(off + 9, next));
				} else if (query.startsWith("orderby=", off)) {
				} else {
					String s = query.substring(off, next);
					int i = s.indexOf('=');
					if (0 <= i && s.length() - i > 1) {
						sb.append(s);
						sb.append('&');
					}
				}
				off = next + 1;
			}
			next = query.length();
			if (query.startsWith("User=", off)) {
				userID = Long.parseLong(query.substring(off + 5, next));
			} else if (query.startsWith("page=", off)) {
				page = Integer.parseInt(query.substring(off + 5, next));
			} else if (query.startsWith("pagesize=", off)) {
				pagesize = Integer.parseInt(query.substring(off + 9, next));
			} else if (query.startsWith("orderby=", off)) {
			} else {
				String s = query.substring(off);
				int i = s.indexOf('=');
				if (0 <= i && s.length() - i > 1) {
					sb.append(s);
				}
			}

			List<Entity> list = null;
			if (userID != null) {
				list = paymentStore.listAll();

				List<Entity> userPayments = new ArrayList<>();
				for (Entity payment : list) {
					Long fromUserID = payment.get("FromUserID");
					Long toUserID = payment.get("ToUserID");
					Long fromAccountType = payment.get("FromAccountTypeID");
					Long toAccountType = payment.get("ToAccountTypeID");
					if (userID.equals(fromUserID) && (fromAccountType.equals(3L) || fromAccountType.equals(1L))) {
						userPayments.add(payment);
					} else if (userID.equals(toUserID) && toAccountType.equals(3L) || toAccountType.equals(1L)) {
						userPayments.add(payment);
					}
				}
				list = userPayments;
			} else {
				return new byte[0];
			}
			if (page > 0) {
				int size = list.size();
				int from = (page - 1) * pagesize;
				int to = from + pagesize;
				to = to < list.size() ? to : list.size();

				if (from < list.size()) {
					int dFrom = size - to;
					int dTo = size - from;

					list = list.subList(dFrom, dTo);
				} else {
					list = new ArrayList<Entity>(0);
				}
			}
			cache = buildFrom(list);
		}
		this.lastModified = System.currentTimeMillis();
		this.cache = cache;
		return cache;
	}

	protected byte[] buildFrom(List<Entity> dataList) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bout);

		try {
			boolean start = true;
			out.append('[');

			for (int i = dataList.size() - 1; i >= 0; i--) {
				Entity data = dataList.get(i);
				if (!start) {
					out.append(',');
				} else {
					start = false;
				}
				jsonHolder.stringifyTo(data, new OutputStreamWriter(out, "utf-8"));
			}
			out.append(']');
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			throw new RuntimeException(e);
		}

		out.flush();
		out.close();
		return bout.toByteArray();
	}

	@Override
	protected String post(HttpServletRequest req) throws IOException {
		return "";
	}

}
