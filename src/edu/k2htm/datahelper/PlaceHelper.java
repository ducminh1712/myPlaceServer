package edu.k2htm.datahelper;

import java.io.File;

public interface PlaceHelper extends DataHelper {
	public void report(String name, String username,short type,long time,int lat,int lng,File image,String comment) throws Exception;
}
