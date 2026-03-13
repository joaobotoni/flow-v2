package com.botoni.flow.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Category implements Parcelable {

    private long id;
    private String description;
    private boolean check;
    public Category() {
    }

    public Category(long id, String description, boolean check) {
        this.id = id;
        this.description = description;
        this.check = check;
    }

    protected Category(Parcel in) {
        id = in.readLong();
        description = in.readString();
        check = in.readByte() != 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(description);
        parcel.writeByte((byte) (check ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && check == category.check && Objects.equals(description, category.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, check);
    }
}
