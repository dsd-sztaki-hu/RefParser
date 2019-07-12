package hu.sztaki.dsd.refparser;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

class CSLDateStruct {

    @SerializedName("date-parts")
    public ArrayList<ArrayList<Integer>> dateParts = new ArrayList<ArrayList<Integer>>();

    public CSLDateStruct(Integer... parts) {
        dateParts.add(new ArrayList<Integer>(Arrays.asList(parts)));
    }

}