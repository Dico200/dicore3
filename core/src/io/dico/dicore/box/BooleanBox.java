package io.dico.dicore.box;

public class BooleanBox {

    public boolean value;

    public BooleanBox() {
    }

    public BooleanBox(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BooleanBox{" +
                "value=" + value +
                '}';
    }
}
