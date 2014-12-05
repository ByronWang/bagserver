package bagserver;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import nebula.data.Entity;
import nebula.http.Application;
import nebula.http.resource.rest.EntityListResouce;

import org.eclipse.jetty.util.URIUtil;
import org.joda.time.DateTime;

public class InventoryListEntityResource extends EntityListResouce {
	final int days;

	public InventoryListEntityResource(Application app) {
		super(app, app.getType("OrderItem"));
		this.days = Integer.parseInt(app.getProperty("app.inventory.days", "30"));
	}

	@Override
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

	private List<Entity> filter(List<Entity> orders) {
		List<Entity> resultOrders = new ArrayList<Entity>();
		for (Entity order : orders) {
			DateTime datatime = order.get("Datetime");
			if (datatime.plusDays(days).isAfterNow()) {
				resultOrders.add(order);
			}
		}
		return resultOrders;
	}

	@Override
	protected byte[] get(HttpServletRequest req) {
		try {
			String query = URIUtil.decodePath(req.getQueryString());
			byte[] cache;
			List<Entity> dataList;
			if (query == null || query.length() == 0) {
				dataList = datastoreHolder.listAll();
				cache = buildFrom(dataList);
			} else {
				int page = -1;
				int pagesize = -1;
				StringBuilder sb = new StringBuilder();
				int next = 0;
				int off = 0;
				while ((next = query.indexOf('&', off)) != -1) {
					if (query.startsWith("page=", off)) {
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
				if (query.startsWith("page=", off)) {
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
				if (sb.length() > 0) {
					DataHolder dataHolder = dataCache.get(sb.toString());
					list = dataHolder.get();
				} else {
					list = datastoreHolder.listAll();
				}
				list = this.filter(list);
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
		} catch (NumberFormatException e) {
			log.error(e);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
}
