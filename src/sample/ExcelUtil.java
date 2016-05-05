package sample;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.net.UrlEscapers;

public class ExcelUtil {

	private static final short IMAGE_ROW_HEIGHT = 4500;
	private static final short DEFAULT_ROW_HEIGHT = 255;
	private static final int IMAGE_COLUMN_INDEX = 9;
	private static final int ID_COLUMN_INDEX = 10;
	
	public static void exportToExcel(List<TyresCollection> tyresCollectionList) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("ErrolsTyres");
		sheet.setDefaultRowHeight(IMAGE_ROW_HEIGHT);
		
		Map<String, Object[]> data = new LinkedHashMap<String, Object[]>();
		data.put("1", new Object[] { "Name", "SKU", "Branch",
				"Tyre Width", "Tyre Profile", "Rim Size", "Load Index",
				"Tyre SI", "Price", "Image", "XXX" });
		
		// prepare data
		int rowNumber = 2;
		for (int i = 0; i < tyresCollectionList.size(); i++) {
			TyresCollection tyresCollection = tyresCollectionList.get(i);
			for (int j = 0; j < tyresCollection.getTyresList().size(); j++) {
				Tyres tyres = tyresCollection.getTyresList().get(j);
				Object[] objArray = new Object[] {
						tyresCollection.getName(),
						tyres.getSku(),
						tyresCollection.getBranch(),
						tyres.getWidth(),
						tyres.getProfile(),
						tyres.getSize(),
						tyres.getLoadIndex(),
						tyres.getSi(),
						tyres.getPrice(), 
						"",
						tyresCollection.getId()
				};
				
				objArray[IMAGE_COLUMN_INDEX] = getImageStream(tyresCollection.getImageUrl());
				data.put(String.valueOf(rowNumber++), objArray);
			}
			
		}
		
		// export data to excel file
		Set<String> keyset = data.keySet();
	    int rownum = 0;
	    List<Integer> imagesId = new ArrayList<>();
	    for (String key : keyset) {
	        Row row = sheet.createRow(rownum);
	        Object [] objArr = data.get(key);
	        int cellnum = 0;
	        for (int i = 0; i < objArr.length; i++) {
	        	Object obj = objArr[i];
	        	
	        	if (i == ID_COLUMN_INDEX) {
	        		continue;
	        	}
	        	
	            Cell cell = row.createCell(cellnum++);
	            if(obj instanceof String)
	                cell.setCellValue((String)obj);
	            else if(obj instanceof Double)
	                cell.setCellValue((Double)obj);
	        }
	        
	        // draw images for images column (last column)
	        if (key.endsWith("1")) {
	        	rownum++;
	        	continue;
	        }
	        
	        Integer id = (Integer) objArr[ID_COLUMN_INDEX];
    		if (imagesId.contains(id)) {
    			row.setHeight(DEFAULT_ROW_HEIGHT);
    			continue;
    		} else {
    			imagesId.add(id);
    		}
	        
			addImages( (InputStream) objArr[IMAGE_COLUMN_INDEX], workbook, sheet, rownum, IMAGE_COLUMN_INDEX);
			row.setHeight(IMAGE_ROW_HEIGHT);
		
			rownum++;
	    }
	    
	    try {
	        FileOutputStream out =new FileOutputStream(new File("D:/MyApps/Tyres/TyresResult.xls"));
	        workbook.write(out);
	        out.close();
	        System.out.println("Excel written successfully..");
	         
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private static InputStream getImageStream(String imageUrl) throws IOException {
		URL url = new URL(UrlEscapers.urlFragmentEscaper().escape(imageUrl));
	    return url.openStream();
	}
	
	private static Dimension addImages(InputStream in, Workbook requestReport, Sheet sheet, int row, int  col) throws IOException {
		Drawing patriarch = sheet.createDrawingPatriarch();
	    CreationHelper helper = requestReport.getCreationHelper();
	    ClientAnchor anchor = helper.createClientAnchor();

	    byte[] bytes = IOUtils.toByteArray(in);
	    int pictureIndex = requestReport.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_PNG);
	  in.close();
	    if (patriarch == null) {
	        patriarch = sheet.createDrawingPatriarch();
	    }
//	    anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
	    anchor.setRow1(row);
	    anchor.setCol1(col);
	    
//	    anchor.setRow2(row + 1);
//	    anchor.setCol2(col + 1);
	    Picture picture = patriarch.createPicture(anchor, pictureIndex);
	    
	    //  to calculate the original height of the image, it use the row.getHeight value
	    picture.resize();
	    return picture.getImageDimension();
//	    picture.setLineStyle(HSSFPicture.LINESTYLE_DASHDOTGEL);
	}

}
