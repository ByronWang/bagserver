package bagserver;

import http.resource.EntityListResouce;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nebula.data.Application;
import nebula.data.DataStore;
import nebula.data.Entity;

import org.eclipse.jetty.util.URIUtil;

public class PurchaserOrderListEntityResource extends EntityListResouce {

    final DataStore<Entity> bidStore;

    public PurchaserOrderListEntityResource(Application app) {
        super(app, app.getType("Order"));
        bidStore = app.getStore("Bid");
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

    private List<Entity> list(Long id) {
        List<Entity> orders = datastoreHolder.listAll();
        List<Entity> resultOrders = new ArrayList<Entity>();
        for (Entity entity : orders) {
            List<Entity> items = entity.get("Items");

            for (Entity item : items) {
                Entity bid = item.getEntity("Bid");
                if (bid != null) {
                    if (id.equals((Long) bid.get("PurchaserID"))) {
                        resultOrders.add(entity);
                        break;
                    }
                }
            }
        }
        return  resultOrders;
    }

    @Override
    protected byte[] get(HttpServletRequest req) {
        String query = URIUtil.decodePath(req.getQueryString());
        byte[] cache;
        List<Entity> dataList;
        if (query == null || query.length() == 0) {
            dataList = datastoreHolder.listAll();
            cache = buildFrom(dataList);
        } else {
            int page = -1;
            int size = -1;
            StringBuilder sb = new StringBuilder();
            int next = 0;
            int off = 0;
            while ((next = query.indexOf('&', off)) != -1) {
                if (query.startsWith("page=", off)) {
                    page = Integer.parseInt(query.substring(off + 5, next));
                } else if (query.startsWith("pagesize=", off)) {
                    size = Integer.parseInt(query.substring(off + 9, next));
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
            Long id =null;
            if (query.startsWith("page=", off)) {
                page = Integer.parseInt(query.substring(off + 5, next));
            } else if (query.startsWith("pagesize=", off)) {
                size = Integer.parseInt(query.substring(off + 9, next));
            } else if (query.startsWith("orderby=", off)) {
            } else if (query.startsWith("Purchaser=", off)) {
                id = Long.parseLong(query.substring(off + "Purchaser=".length(), next));
            } else {
                String s = query.substring(off);
                int i = s.indexOf('=');
                if (0 <= i && s.length() - i > 1) {
                    sb.append(s);
                }
            }
            List<Entity> list = null;
//                if (sb.length() > 0) {
//                    DataHolder dataHolder = dataCache.get(sb.toString());
//                    list = dataHolder.get();
//                } else {
            list = this.list(id);
//                }
            if (page > 0) {
                int from = (page - 1) * size;
                int to = from + size;
                to = to < list.size() ? to : list.size();
                if (from < list.size()) {
                    list = list.subList(from, to);
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
}
