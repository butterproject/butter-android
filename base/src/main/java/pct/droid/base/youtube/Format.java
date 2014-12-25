package pct.droid.base.youtube;

/**
 * Represents a format in the "fmt_list" parameter
 * Currently, only id is used
 */
public class Format {

    protected int mId;

    /**
     * Construct this object from one of the strings in the "fmt_list" parameter
     *
     * @param pFormatString one of the comma separated strings in the "fmt_list" parameter
     */
    public Format(String pFormatString) {
        String formatVars[] = pFormatString.split("/");
        mId = Integer.parseInt(formatVars[0]);
    }

    /**
     * Construct this object using a format id
     *
     * @param id id of this format
     */
    public Format(int id) {
        this.mId = id;
    }

    /**
     * Retrieve the id of this format
     *
     * @return the id
     */
    public int getId() {
        return mId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Format)) {
            return false;
        }
        return ((Format) object).mId == mId;
    }

}
