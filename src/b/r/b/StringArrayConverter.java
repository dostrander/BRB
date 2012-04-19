//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store the child ids of a parent message in one string then
//unpack it later for pulling purposes
package b.r.b;

public class StringArrayConverter {
	public String convertArrayToString(String[] array){
    	String str = "";
    	for(int i = 0;i<array.length;i++){
    		str=str+array[i];
    		if(i<array.length-1){
    			str = str + ",";
    		}
    	}
    	return str;
    }
    public  String[] convertStringToArray(String str){
    	String[] array = str.split(",");
    	return array;
    }
}
