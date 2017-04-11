/**
 * Created by xinghe on 2017/4/8.
 */
//This class is used to convert byte[] and int, and the code comes from website https://wangzzu.github.io/2015/10/27/TheTransformOfJava/#int与Byte的相互转换
class Helper
{

    //Convert byte array to integer
    static int byteToInt(byte[] byteArray)
    {
        int val=0, i;
        i=0;
        while (byteArray.length > i) {
            int shift= (3 - i) << 3;
            val = val + ((byteArray[i] & 0x000000FF) << shift);
            i++;
        }
        return val;
    }

    //Convert integer to byte array

    static byte[] intToByte(int number)
    {
        byte[] result;
        result = new byte[4];
        result[0] = (byte) (number / 16777216);
        result[1] = (byte) (number / 65536);
        result[2] = (byte) (number / 256);
        result[3] = (byte) number;
        return result;
    }


    static int upperNum(int dividend, int divisor)
    {
        int result;
        result = dividend / divisor;
        return result * divisor == dividend ? result : result + 1;
    }

    static ActMsg byteToMsg(byte[] temp)
    {
        int messageType,i;
        byte[] payload=new byte[temp.length-5];
        messageType=(int)temp[4];
        i=5;
        while (i < temp.length) {
            payload[i-5]=temp[i];
            i++;
        }
        return(new ActMsg(messageType, payload));
    }

}

