package bagserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import nebula.data.DataSession;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.TransactionCaller;
import nebula.data.impl.EditableEntity;
import nebula.http.Application;
import nebula.http.resource.rest.EntityListResouce;
import util.FileUtil;

public class BidListEntityResource extends EntityListResouce {
    final DataStore<Entity> itemStore;

    public BidListEntityResource(Application app) {
        super(app, app.getType("Bid"));
        itemStore = app.getStore("OrderItem");
    }

    @Override
    protected String post(HttpServletRequest req) throws IOException {
        InputStream in = req.getInputStream();
        if (log.isTraceEnabled()) {
            in = FileUtil.print(in);
        }
        final EditableEntity bid = jsonHolder.readFrom(new EditableEntity(), new InputStreamReader(in, "utf-8"));

        try {
            this.datastoreHolder.save(new TransactionCaller() {

                @Override
                public void exec(DataSession session) throws Exception {
                    Long itemID = bid.get("OrderItemID");

                    EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();
                    BigDecimal actualPrice = bid.get("SuggestedPrice");
                    BigDecimal quantity = new BigDecimal((Long) item.get("Quantity"));
                    BigDecimal commission = bid.get("Commission");
                    BigDecimal deliveryCost = bid.get("DeliveryCost");
                    if(deliveryCost==null){
                    	deliveryCost = new BigDecimal(0);
                    }

                    BigDecimal productAmount = actualPrice.multiply(quantity);
                    BigDecimal productCommissionAmount = productAmount.multiply(commission.divide(new BigDecimal(100)));
                    BigDecimal amount = productAmount.add(productCommissionAmount).add(deliveryCost);

                    bid.put("ProductAmount", productAmount);
                    bid.put("ProductCommissionAmount", productCommissionAmount);
                    bid.put("Amount", amount);
                    Long cnt = item.get("SuitorCount");
                    if (cnt != null) {
                        cnt += 1;
                    } else {
                        cnt = 1L;
                    }
                    item.put("SuitorCount", cnt);

                    session.add(itemStore, item);
                    session.add(datastoreHolder, bid);
                    session.flush();
                }
            });
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return "{\"ID\":" + bid.getID() + "}";
    }
}
