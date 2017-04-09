/**
 * Created by xinghe on 2017/4/8.
 */
//This class is used to convert byte[] and int, and the code comes from website https://wangzzu.github.io/2015/10/27/TheTransformOfJava/#int与Byte的相互转换
public class Helper
{

    //Convert byte array to integer
    public static int byteToInt(byte[] temp)
    {
        int val=0, i=0;
		for(i=0;i<temp.length;i++){
			int shift= (4 - 1 - i) * 8;
            val +=(temp[i] & 0x000000FF) << shift;
		}
		return val;
    }

    //Convert integer to byte array

    public static byte[] intToByte(int number)
    {
        byte[] result = new byte[4];
        result[0] = (byte) (number >> 24);
        result[1] = (byte) (number >> 16);
        result[2] = (byte) (number >> 8);
        result[3] = (byte) (number);
        return result;
    }


    public static int upperNum(int dividend, int divisor)
    {
        int result = dividend / divisor;
        if(result * divisor == dividend) {
            return result;
        } else {
            return result + 1;
        }
    }

}

