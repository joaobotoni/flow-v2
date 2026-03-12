package com.botoni.flow.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Transport implements Parcelable {
    private String name;
    private Integer quantity;
    private Integer initialCapacity;
    private Integer finalCapacity;

    public Transport() {
    }

    public Transport(String name, Integer quantity, Integer initialCapacity, Integer finalCapacity) {
        this.name = name;
        this.quantity = quantity;
        this.initialCapacity = initialCapacity;
        this.finalCapacity = finalCapacity;
    }

    protected Transport(Parcel in) {
        name = in.readString();
        if (in.readByte() == 0) {
            quantity = null;
        } else {
            quantity = in.readInt();
        }
        if (in.readByte() == 0) {
            initialCapacity = null;
        } else {
            initialCapacity = in.readInt();
        }
        if (in.readByte() == 0) {
            finalCapacity = null;
        } else {
            finalCapacity = in.readInt();
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

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        if (quantity == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(quantity);
        }
        if (initialCapacity == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(initialCapacity);
        }
        if (finalCapacity == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(finalCapacity);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public Integer getFinalCapacity() {
        return finalCapacity;
    }

    public void setFinalCapacity(Integer finalCapacity) {
        this.finalCapacity = finalCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transport transport = (Transport) o;
        return Objects.equals(name, transport.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
