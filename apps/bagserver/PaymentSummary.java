package bagserver;

import java.math.BigDecimal;

import nebula.data.DataStore;
import nebula.data.Entity;

public class PaymentSummary {
    final DataStore<Entity> paymentStore;

    PaymentSummary(final DataStore<Entity> paymentStore) {
        this.paymentStore = paymentStore;
    }

    BigDecimal summaryPersonal(Long userID) {
        BigDecimal sumFrom = new BigDecimal(0);
        BigDecimal sumTo = new BigDecimal(0);
        for (Entity payment : this.paymentStore.listAll()) {
            Long fromUserID = payment.get("FromUserID");
            Long toUserID = payment.get("ToUserID");
            Long fromAccountType = payment.get("FromAccountTypeID");
            Long toAccountType = payment.get("toAccountTypeID");
            BigDecimal amount = payment.get("Amount");

            if (userID.equals(fromUserID) && fromAccountType.equals(1L)) {
                sumFrom.add(amount);
            } else if (userID.equals(toUserID) && toAccountType.equals(1L)) {
                sumTo.add(amount);
            }
        }
        return sumTo.min(sumFrom);
    }

    BigDecimal summaryDepositl(Long userID) {
        BigDecimal sumFrom = new BigDecimal(0);
        BigDecimal sumTo = new BigDecimal(0);
        for (Entity payment : this.paymentStore.listAll()) {
            Long fromUserID = payment.get("FromUserID");
            Long toUserID = payment.get("ToUserID");
            Long fromAccountType = payment.get("FromAccountTypeID");
            Long toAccountType = payment.get("toAccountTypeID");
            BigDecimal amount = payment.get("Amount");

            if (userID.equals(fromUserID) && fromAccountType.equals(3L)) {
                sumFrom.add(amount);
            } else if (userID.equals(toUserID) && toAccountType.equals(3L)) {
                sumTo.add(amount);
            }
        }
        return sumTo.min(sumFrom);
    }
}
