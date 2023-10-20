import java.util.ArrayList;
import java.util.Arrays;

public class Wrapper {
    static int n2 = 5; //разрядность функции
    static int[] func2 = string_to_array("00101000101100010010100010110001");//вектор
    static int n3 = 5; //разрядность функции
    static int[] func3 = string_to_array("10111111111110111010011110100101");//вектор
    static int n4 = 4; //разрядность функции
    static int[] func4 = string_to_array("1111101110100111");//вектор

    public static void main(String[] args) {
        ArrayList<int[]> result = Run.quineMcCluskey(func3, n3);
        ArrayList<String> res_in_letters = convertToLettersForm(result);
        for (int i = 0; i < result.size(); i++){
            System.out.print(Arrays.toString(result.get(i)) + " ");
        }
        System.out.println();
        System.out.println(res_in_letters);
    }

    public static ArrayList<String> calculate(String vector, int capacity){
        ArrayList<int[]> result = Run.quineMcCluskey(string_to_array(vector), capacity);
        return convertToLettersForm(result);
    }

    private static int[] string_to_array(String str){
        int[] arr = new int[str.length()];
        for (int i = 0; i < str.length(); i++){
            arr[i] = Integer.parseInt(String.valueOf(str.charAt(i)));
        }
        return arr;
    }

    private static ArrayList<String> convertToLettersForm(ArrayList<int[]> bitForm){
        ArrayList<String> res = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (int[] ints : bitForm) {
            for (int j = 0; j < ints.length - 1; j++) {
                if (ints[j] != 3) {
                    switch (j) {
                        case 0 -> buf.append(ints[j] == 0 ? "a" : "A");
                        case 1 -> buf.append(ints[j] == 0 ? "b" : "B");
                        case 2 -> buf.append(ints[j] == 0 ? "c" : "C");
                        case 3 -> buf.append(ints[j] == 0 ? "d" : "D");
                        case 4 -> buf.append(ints[j] == 0 ? "e" : "E");
                    }
                }
            }
            res.add(buf.toString());
            buf = new StringBuilder();
        }
        return res;
    }
}
