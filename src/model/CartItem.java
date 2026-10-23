package model;

import java.math.BigDecimal;

public class CartItem {
    private final Medicine medicine;
    private int quantity;
 
    public CartItem(Medicine medicine, int quantity) {
        this.medicine = medicine;
        this.quantity = quantity;
    }
 
    public Medicine getMedicine() { return medicine; }
 
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
 
    public BigDecimal getLineTotal() {
        return medicine.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
