import java.util.Arrays;

public class Main {
    //Ввод данных с платы:
    static int n0 = 5; //разрядность функции
    static int[] func = {0,1,1,0,0,1,1,1,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0};//вектор

    static int n1 = 5; //разрядность функции
    static int[] func1 = {1,1,1,0,0,1,1,1,1,1,0,1,1,1,0,0,0,1,0,0,1,1,0,0,0,1,0,0,1,1,0,0};//вектор

    static int n2 = 5; //разрядность функции
    static int[] func2 = string_to_array("00101000101100010010100010110001");//вектор

    static int n3 = 5; //разрядность функции
    static int[] func3 = string_to_array("01100111110011001100110011001100");//вектор

    static int n4 = 2; //разрядность функции
    static int[] func4 = string_to_array("0110");//вектор

    public static void main(String[] args) {
        //quineMcCluskey(func, n);
        quineMcCluskey(func4, n4);
    }

    private static int[] string_to_array(String str){
        int[] arr = new int[str.length()];
        for (int i = 0; i < str.length(); i++){
            arr[i] = Integer.parseInt(String.valueOf(str.charAt(i)));
        }
        return arr;
    }

    static void quineMcCluskey(int[] func, int n){
        int k = (int) Math.pow(2, n);
        //Массив регистров со всеми импилкантами, размер одного регситра - n, размер массива 2**n
        int[][] implicants = new int[k][n];
        int ci; //Кол-во импликант

        //Получаем список импликант
        ci = 0;
        String binary;
        int bs;
        for(int i = 0; i < k; i++){
            if(func[i] == 1) {
                binary = Integer.toBinaryString(i);
                bs = binary.length();
                for(int j = n - bs; j < n; j++){
                    implicants[ci][j] = Integer.parseInt(binary.substring(j - (n - bs), j+1 - (n - bs)));
                }
                ci++;
            }
        }


        //Группы
        int[][][][] groups = new int[6][k][k][n+1];


        //Распределение по группам
        //Группа с 5 переменными
        int c;
        int[][] cn = new int[6][n+1];

        for(int i = 0; i < ci; i++){
            c = count_of_1(implicants[i], n);
            for(int j = 0; j < n; j++){ //Это ок, потому что системная модель пишется с учетом будущей реализации на verilog
                groups[0][c][cn[0][c]][j] = implicants[i][j];
            }
            cn[0][c]++;
        }

        //Группа с 4 переменными и одной *
        int local_count = 0;
        int cmp_out; //result of compare two numbers
        int[] local; //new number got by merging
        int cmp_d; //compare down
        int cmp_u; //compare up
        boolean wf; //write flag
        int ml = 0; //merging level
        boolean cf = true; //comparing flag used to show that it was comparing on this comparing level

        //Склеивание подгрупп в цикле

        while(cf) {
            cf = false;

            for (cmp_u = 0; cmp_u < n; cmp_u++) {
                cmp_d = cmp_u + 1;
                for (int i = 0; i < cn[ml][cmp_u]; i++) {
                    for (int j = 0; j < cn[ml][cmp_d]; j++) {
                        //Функции сравнения вынести в виде task'ов
                        cmp_out = compare_for_merging(groups[ml][cmp_u][i], groups[ml][cmp_d][j], n); //больше 0 если можно склеить
                        if (cmp_out >= 0) {
                            cf = true;
                            local = groups[ml][cmp_u][i].clone();
                            local[cmp_out] = 3;
                            local[n] = 0;

                            groups[ml][cmp_u][i][n] = 4;
                            groups[ml][cmp_d][j][n] = 4;

                            c = count_of_1(local, n);

                            wf = true;
                            for (int p = 0; p < cn[ml + 1][c]; p++) {
                                if (compare_implicants(local, groups[ml + 1][c][p], n)) {
                                    wf = false;
                                    break;
                                }
                            }

                            if (wf) {
                                groups[ml + 1][c][cn[ml + 1][c]] = local;
                                cn[ml + 1][c]++;
                            }
                        }
                    }
                }
            }

            ml++;
        }


        //output_cmd(groups, cn);

        //Таблица Квайна
        int[][] quine_table = new int[32][32];

        //Массив простых импликант
        int[][] pi = new int[32][6]; //prime implicants

        //Поиск всех простых импликант
        int cpi = 0; //count of prime implicants

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int b = 0; b < cn[i][j]; b++){
                    if (groups[i][j][b][n] == 0){
                        pi[cpi] = groups[i][j][b].clone();
                        cpi++;
                    }
                }
            }
        }

        //Массив 1-точек функции
        int[][] p1 = new int[32][6];

        int cp1 = 0; //count of point 1

        for (int i = 0; i < n; i++){
            for (int j = 0; j < cn[0][i]; j++) {
                p1[cp1] = groups[0][i][j].clone();
                p1[cp1][n] = 0;
                cp1++;
            }
        }

        //Заполнение таблицы Квайна
        for(int i = 0; i < ci; i++){
            for(int j = 0; j < cpi; j++){
                if (compare_for_q_table(p1[i], pi[j], n))
                    quine_table[i][j] = 1;
            }
        }

        //Поиск ядерных импликант
        int cicr; //core_implicant_check result
        int cci = 0;
        for(int i = 0; i < ci; i++){
            cicr = core_implicant_check(quine_table[i], cpi);
            if(cicr >= 0) {
                pi[cicr][n] = 4;
                cci++;
            }
        }

        //Отмечаем строки, перекрытые ядерными импликантами
        for (int i = 0; i < cpi; i++){
            if(pi[i][n] == 4){
                for (int j = 0; j < cp1; j++){
                    if (quine_table[j][i] == 1){
                        p1[j][n] = 4;
                    }
                }
            }
        }
        //Создадим отдельную табличку для Петрика из оставшихся простых импликант
        int[] c1pt = new int[32];
        int[][][] petrick_table = new int[32][32][32];
        int l = 0; //petrick table level
        for (int i = 0; i < cp1; i++){
            if(p1[i][n] == 0) {
                petrick_table[0][c1pt[0]] = quine_table[i].clone();
                c1pt[0]++;
            }
        }
        //Далее будем использовать одну из вариаций метода петрика.
        int pei = 0;//position of extra implicant
        int cei = 0; //count of extra implicants
        int c1c = 0; //count of 1 in column
        int max_c1c = 0;

        while(true) {
            for (int i = 0; i < cpi; i++) {
                for (int j = 0; j < c1pt[l]; j++) {
                    if (petrick_table[l][j][i] == 1) {
                        c1c++;
                    }
                }
                if (c1c > max_c1c) {
                    max_c1c = c1c;
                    pei = i;
                }
            }
            pi[pei][n] = 4;
            for (int i = 0; i < c1pt[l]; i++) {
                if (petrick_table[l][i][pei] != 1) {
                    petrick_table[l + 1][c1pt[l + 1]] = petrick_table[l][i].clone();
                    c1pt[l + 1]++;
                }
            }
            if (c1pt[l + 1] == 0) {
                break;
            }
            l++;
            c1c = 0;
            max_c1c = 0;
        }


        //Вывод таблицы Квайна
        /*for (int i = 0; i < cpi; i++){
            System.out.print(pi[i][0]+""+pi[i][1]+""+pi[i][2]+""+pi[i][3]+""+pi[i][4]+""+pi[i][5]+" ");
        }*/
        System.out.println();
        for(int i = 0; i < ci; i++){
            System.out.print(Arrays.toString(p1[i]) + " ");
            for(int j = 0; j < cpi; j++){
                System.out.print(quine_table[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        //Вывод Петрика
        for (int i = 0; i < c1pt[0]; i++){
            for (int j = 0; j < cpi; j++){
                System.out.print(petrick_table[0][i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < cpi; i++){
            if(pi[i][n] == 4) {
                System.out.print(Arrays.toString(pi[i]) + " ");
            }
        }

    }

    static void output_cmd(int[][][][] groups0, int[][] cn, int n){
        for(int r = 0; r < 6; r++) {
            for (int i = 0; i <= n; i++) {
                for (int j = 0; j < cn[r][i]; j++) {
                    for (int t = 0; t < n; t++) {
                        System.out.print(groups0[r][i][j][t] + " ");
                    }
                    System.out.print(" " + groups0[r][i][j][n]);
                    System.out.println();
                }
                System.out.println();
            }
        }
    }

    static int count_of_1(int[] bin_num, int n){
        int c = 0;
        for (int j = 0; j < n; j++){
            if (bin_num[j] == 1)
                c++;
        }
        return c;
    }

    static int core_implicant_check(int[] a, int csi){
        int pos = 0;
        int c = 0;
        for (int i = 0; i < csi; i++){
            if(a[i] == 1) {
                c++;
                pos = i;
            }
        }
        if (c == 1)
            return pos;
        else
            return -1;
    }


    static int compare_for_merging(int[] f1, int[] f2, int n){
        int local_count = 0;
        int t_pos = 0;
        for(int t = 0; t < n; t++){
            if (f1[t] != f2[t]){
                local_count++;
                t_pos = t;
            }
        }
        if (local_count == 1)
            return t_pos;
        else
            return -1;
    }

    static boolean compare_implicants(int[] f1, int[] f2, int n){
        for (int i = 0; i < n; i++){
            if (f1[i] != f2[i]){
                return false;
            }
        }
        return true;
    }

    static boolean compare_for_q_table(int[] p1, int[] si, int n) {
        for (int i = 0; i < n; i++){
            if (!(p1[i] == si[i] || si[i] == 3)){
                return false;
            }
        }
        return true;
    }
}
