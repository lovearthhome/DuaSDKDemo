package net.rimoto.intlphoneinput;

import android.os.Parcel;
import android.os.Parcelable;

public class Country implements Parcelable {
    /**
     * Name of country
     */
    private String name;

    /**
     * EnglishName of country
     */
    private String nameEnglish;

    /**
     * EnglishName of country
     */
    private String nameChinese;
    /**
     * ISO2 of country
     */
    private String iso;
    /**
     * Dial code prefix of country
     */
    private int dialCode;


    /**
     * Constructor
     *
     * @param name     String
     * @param iso      String of ISO2
     * @param dialCode int
     */
    public Country(String name, String iso, int dialCode) {
        setName(name);
        setIso(iso);
        setDialCode(dialCode);
    }

    /**
     * Get name of country
     *
     * @return String
     */
    public String getNameInEnglish() {
        return name;
    }
    /**
     * Get name of country
     *
     * @return String
     */
    public String getNameInChinese() {
        return name;
    }
    /**
     * Get name of country
     *
     * @return String
     */
    public String getName() {
        return name;
    }
    /**
     * Set name of country
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get ISO2 of country
     *
     * @return String
     */
    public String getIso() {
        return iso;
    }

    /**
     * Set ISO2 of country
     *
     * @param iso String
     */
    public void setIso(String iso) {
        this.iso = iso.toUpperCase();
    }

    /**
     * Get dial code prefix of country (like +1)
     *
     * @return int
     */
    public int getDialCode() {
        return dialCode;
    }

    /**
     * Set dial code prefix of country (like +1)
     *
     * @param dialCode int (without + prefix!)
     */
    public void setDialCode(int dialCode) {
        this.dialCode = dialCode;
    }

    /**
     * Check if equals
     *
     * @param o Object to compare
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Country) && (((Country) o).getIso().toUpperCase().equals(this.getIso().toUpperCase()));
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iso);
        dest.writeString(this.name);
        dest.writeInt(this.dialCode);
//        dest.writeString(this.mcc);
//        dest.writeString(this.mnc);
        dest.writeString(this.name);
//        dest.writeStringList(this.phonePatterns);
//        dest.writeStringList(this.phonePatternGroups);
    }

    public Country() {
    }

    protected Country(Parcel in) {
        this.iso = in.readString();
        this.name = in.readString();
        this.dialCode = in.readInt();
//        this.mcc = in.readString();
//        this.mnc = in.readString();
        this.name = in.readString();
//        this.phonePatterns = in.createStringArrayList();
//        this.phonePatternGroups = in.createStringArrayList();
    }

    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel source) {
            return new Country(source);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}
