package com.botoni.flow.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Transport implements Parcelable {
    private long id;
    private String name;
    private Integer quantity;
    private Integer percent;
    private Integer capacity;

    public Transport() {
    }

    public Transport(long id, String name, Integer quantity, Integer capacity, Integer percent) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.capacity = capacity;
        this.percent = percent;
    }

    protected Transport(Parcel in) {
        id = in.readLong();
        name = in.readString();
        quantity = in.readByte() == 0 ? null : in.readInt();
        capacity = in.readByte() == 0 ? null : in.readInt();
        percent = in.readByte() == 0 ? null : in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        writeNullableInt(parcel, quantity);
        writeNullableInt(parcel, capacity);
        writeNullableInt(parcel, percent);
    }

    private void writeNullableInt(Parcel parcel, Integer value) {
        if (value == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(value);
        }
    }

    public static final Creator<Transport> CREATOR = new Creator<Transport>() {
        @Override
        public Transport createFromParcel(Parcel in) {
            return new Transport(in);
        }

        @Override
        public Transport[] newArray(int size) {
            return new Transport[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getPercent() { return percent; }
    public void setPercent(Integer percent) { this.percent = percent; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transport transport = (Transport) o;
        return id == transport.id
                && Objects.equals(name, transport.name)
                && Objects.equals(quantity, transport.quantity)
                && Objects.equals(percent, transport.percent)
                && Objects.equals(capacity, transport.capacity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, percent, capacity);
    }
}