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
            c = count_of_1(implicants[i]);
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

            for (cmp_u = 0; cmp_u < 5; cmp_u++) {
                cmp_d = cmp_u + 1;
                for (int i = 0; i < cn[ml][cmp_u]; i++) {
                    for (int j = 0; j < cn[ml][cmp_d]; j++) {
                        //Функции сравнения вынести в виде task'ов
                        cmp_out = compare_for_merging(groups[ml][cmp_u][i], groups[ml][cmp_d][j]); //больше 0 если можно склеить
                        if (cmp_out >= 0) {
                            cf = true;
                            local = groups[ml][cmp_u][i].clone();
                            local[cmp_out] = 3;
                            local[n] = 0;

                            groups[ml][cmp_u][i][n] = 4;
                            groups[ml][cmp_d][j][n] = 4;

                            c = count_of_1(local);

                            wf = true;
                            for (int p = 0; p < cn[ml + 1][c]; p++) {
                                if (compare_implicants(local, groups[ml + 1][c][p])) {
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
        int[][] si = new int[32][6];

        //Поиск всех простых импликант
        int csi = 0; //count of simple implicants

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int b = 0; b < cn[i][j]; b++){
                    if (groups[i][j][b][n] == 0){
                        si[csi] = groups[i][j][b].clone();
                        csi++;
                    }
                }
            }
        }

        //Массив 1-точек функции
        int[][] p1 = new int[32][6];

        int cp1 = 0; //count of point 1

        for (int i = 0; i < 6; i++){
            for (int j = 0; j < cn[0][i]; j++) {
                p1[cp1] = groups[0][i][j].clone();
                cp1++;
            }
        }

        //Заполнение таблицы Квайна
        for(int i = 0; i < ci; i++){
            for(int j = 0; j < csi; j++){
                if (compare_for_q_table(p1[i], si[j]))
                    quine_table[i][j] = 1;
            }
        }

        //Поиск ядерных импликант
        int cicr; //core_implicant_check result
        for(int i = 0; i < ci; i++){
            cicr = core_implicant_check(quine_table[i], csi);
            if(cicr >= 0) {
                si[cicr][n] = 4;
            }
        }

        //Вывод таблицы Квайна
        for (int i = 0; i < csi; i++){
            System.out.print(si[i][0]+""+si[i][1]+""+si[i][2]+""+si[i][3]+""+si[i][4]+""+si[i][5]+" ");
        }
        System.out.println();
        for(int i = 0; i < ci; i++){
            for(int j = 0; j < csi; j++){
                System.out.print(quine_table[i][j] + " ");
            }
            System.out.println();
        }

        //Далее будем использовать одну из вариаций метода петрика.

    }

    static void output_cmd(int[][][][] groups0, int[][] cn){
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

    static boolean compare_for_q_table(int[] p1, int[] si) {
        for (int i = 0; i < 5; i++){
            if (!(p1[i] == si[i] || si[i] == 3)){
                return false;
            }
        }
        return true;
    }
}
