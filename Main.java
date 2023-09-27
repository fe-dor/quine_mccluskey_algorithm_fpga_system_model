public class Main {
    //Ввод данных с платы:
    static int n = 5; //разрядность функции
    static int[] func = {0,1,1,0,0,1,1,1,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0};//вектор

    public static void main(String[] args) {
        quineMcCluskey(func, n);
    }

    static void quineMcCluskey(int[] func, int n){
        int k = (int) Math.pow(2, n);
        //Массив регистров со всеми импилкантами, размер одного регситра - n, размер массива 2**n
        int[][] implicants = new int[k][n];
        int ci; //Кол-во импликант

        //Получаем список импликант
        ci = 0;
        String binary;
        int bs = 0;
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
        int[][][][] groups0 = new int[6][k][k][n+1];


        //Распределение по группам
        //Группа с 5 переменными
        int c;
        int[][] cn = new int[6][n+1];

        for(int i = 0; i < ci; i++){
            c = count_of_1(implicants[i]);
            for(int j = 0; j < n; j++){
                groups0[0][c][cn[0][c]][j] = implicants[i][j];
            }
            cn[0][c]++;
        }

        //Группа с 4 переменными и одной *
        int local_count = 0;
        int cmp_out = 0; //result of compare two numbers
        int[] local = new int[n+1]; //new number got by merging
        int cmp_d = 0; //compare down
        int cmp_u = 0; //compare up
        boolean wf = true; //write flag
        int ml = 0; //merging level
        boolean cf = true; //comparing flag used to show that it was comparing on this comparing level

        int[][][] groups1 = new int[k][k*2][n+1];
        //Склеивание первой подгруппы внутри себя

        while(cf) {
            cf = false;

            for (cmp_u = 0; cmp_u < 5; cmp_u++) {
                cmp_d = cmp_u + 1;
                for (int i = 0; i < cn[ml][cmp_u]; i++) {
                    for (int j = 0; j < cn[ml][cmp_d]; j++) {
                        cmp_out = compare_for_merging(groups0[ml][cmp_u][i], groups0[ml][cmp_d][j]); //больше 0 если можно склеить
                        if (cmp_out >= 0) {
                            cf = true;
                            local = groups0[ml][cmp_u][i].clone();
                            local[cmp_out] = 3;
                            local[n] = 0;

                            groups0[ml][cmp_u][i][n] = 4;
                            groups0[ml][cmp_d][j][n] = 4;

                            c = count_of_1(local);

                            wf = true;
                            for (int p = 0; p < cn[ml + 1][c]; p++) {
                                if (compare_implicants(local, groups0[ml + 1][c][p])) {
                                    wf = false;
                                    break;
                                }
                            }

                            if (wf) {
                                groups0[ml + 1][c][cn[ml + 1][c]] = local;
                                cn[ml + 1][c]++;
                            }
                        }
                    }
                }
            }

            ml++;
        }

        /*

        int[][][] groups2 = new int[k][k*2][n+1];
        //Склеивание второй подгруппы внутри себя
        for(cmp_u = 0; cmp_u < 5; cmp_u++) {
            cmp_d = cmp_u + 1;
            for (int i = 0; i < cn[1][cmp_u]; i++) {
                for (int j = 0; j < cn[1][cmp_d]; j++) {
                    cmp_out = compare_for_merging(groups1[cmp_u][i], groups1[cmp_d][j]); //больше 0 если можно склеить
                    if (cmp_out >= 0) {
                        local = groups1[cmp_u][i].clone();
                        local[cmp_out] = 3;
                        local[n] = 0;

                        groups1[cmp_u][i][n] = 4;
                        groups1[cmp_d][j][n] = 4;

                        c = count_of_1(local);

                        wf = true;
                        for (int p = 0; p < cn[2][c]; p++) {
                            if (compare_implicants(local, groups2[c][p])) {
                                wf = false;
                                break;
                            }
                        }

                        if (wf) {
                            groups2[c][cn[2][c]] = local;
                            cn[2][c]++;
                        }
                    }
                }
            }
        }

        int[][][] groups3 = new int[k][k*2][n+1];
        //Склеивание второй подгруппы внутри себя
        for(cmp_u = 0; cmp_u < 5; cmp_u++) {
            cmp_d = cmp_u + 1;
            for (int i = 0; i < cn[2][cmp_u]; i++) {
                for (int j = 0; j < cn[2][cmp_d]; j++) {
                    cmp_out = compare_for_merging(groups2[cmp_u][i], groups2[cmp_d][j]); //больше 0 если можно склеить
                    if (cmp_out >= 0) {
                        local = groups2[cmp_u][i].clone();
                        local[cmp_out] = 3;
                        local[n] = 0;

                        groups2[cmp_u][i][n] = 4;
                        groups2[cmp_d][j][n] = 4;

                        c = count_of_1(local);

                        wf = true;
                        for (int p = 0; p < cn[3][c]; p++) {
                            if (compare_implicants(local, groups3[c][p])) {
                                wf = false;
                                break;
                            }
                        }

                        if (wf) {
                            groups3[c][cn[3][c]] = local;
                            cn[3][c]++;
                        }
                    }
                }
            }
        }


*/




//            for (int i = 0; i < implicants.length; i++){
//                for (int j = 0; j < implicants[0].length; j++){
//                    System.out.print(implicants[i][j] + " ");
//                }
//                System.out.println();
//            }


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

    static int count_of_1(int[] bin_num){
        int c = 0;
        for (int j = 0; j < n; j++){
            if (bin_num[j] == 1)
                c++;
        }
        return c;
    }

    static int compare_for_merging(int[] f1, int[] f2){
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

    static boolean compare_implicants(int[] f1, int[] f2){
        for (int i = 0; i < n; i++){
            if (f1[i] != f2[i]){
                return false;
            }
        }
        return true;
    }
}
