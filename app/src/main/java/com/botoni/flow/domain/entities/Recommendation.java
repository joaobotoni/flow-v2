package com.botoni.flow.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Recommendation implements Parcelable {
    private String description;
    private int initialQuantity;
    private int finalQuantity;
    public Recommendation() {
    }
    public Recommendation(String description, int initialQuantity, int finalQuantity) {
        this.description = description;
        this.initialQuantity = initialQuantity;
        this.finalQuantity = finalQuantity;
    }

    protected Recommendation(Parcel in) {
        description = in.readString();
        initialQuantity = in.readInt();
        finalQuantity = in.readInt();
    }

    public static final Creator<Recommendation> CREATOR = new Creator<Recommendation>() {
        @Override
        public Recommendation createFromParcel(Parcel in) {
            return new Recommendation(in);
        }

        @Override
        public Recommendation[] newArray(int size) {
            return new Recommendation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeInt(initialQuantity);
        parcel.writeInt(finalQuantity);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public int getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(int finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description);
    }
}
