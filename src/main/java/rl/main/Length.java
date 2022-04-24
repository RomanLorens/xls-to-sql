package rl.main;

public class Length {
    int min;
    int max;

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "Length{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
