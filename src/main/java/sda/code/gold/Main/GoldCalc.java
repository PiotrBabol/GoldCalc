package sda.code.gold.Main;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sda.code.gold.GoldPrice;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class GoldCalc {
    private final static String json = "/?format=json";

    public static void main(String[] args) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.nbp.pl/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoldInterface goldInterface = retrofit.create(GoldInterface.class);

        System.out.println("、ヽ｀、ヽ｀°˖✧◝(⁰▿⁰)◜✧˖°、ヽ｀、ヽ｀个o(･_･｡)｀ヽ、｀ヽ、");
        LocalDate localDate1 = LocalDate.of(2017, 12, 03);
        LocalDate localDate2 = LocalDate.of(2018, 02, 03);

        try (Scanner cin = new Scanner(System.in)) {

            LocalDate localDate1st;
            LocalDate localDate2nd;

            System.out.println("1st :");
            localDate1st = LocalDate.parse(cin.nextLine());
            System.out.println("2nd :");
            localDate2nd = LocalDate.parse(cin.nextLine());
            System.out.println("File name :");
            String nameOfFile = cin.nextLine();

            if (localDate2nd.toEpochDay() - localDate1st.toEpochDay() > 367) {
                throw new IllegalArgumentException("The period is greater than 367 days  ｡･ﾟﾟ･(>д<)･ﾟﾟ･｡");
            }

            printToCSV(getGoldPrices(goldInterface, localDate2nd, localDate1st), nameOfFile, localDate2nd);

        } catch (Exception e) {

            throw e;
        }


        List<GoldPrice> goldResponce1 = getGoldPrices(goldInterface, localDate2, localDate1);
        OptionalDouble average1 = getOptionalDouble(goldResponce1);
        System.out.println("average  " + (average1.isPresent() ? average1.getAsDouble() : 0));

        System.out.println("、ヽ｀、ヽ｀°˖✧◝(⁰▿⁰)◜✧˖°、ヽ｀、ヽ｀个o(･_･｡)｀ヽ、｀ヽ、");

        checkIfWorthSelling(average1, localDate2);

        printToCSV(goldResponce1, "AFTER.csv", localDate2);


    }

    private static void printToCSV(List<GoldPrice> goldResponce1, String name, LocalDate localDate) throws IOException {
        File file = new File(name);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        CSVPrinter printer = CSVFormat.RFC4180.withHeader("Data", "Price").print(writer);

        List<GoldPrice> listOfGoldPrice = goldResponce1;

        for (GoldPrice goldPrice : listOfGoldPrice) {
            printer.printRecord(goldPrice.getData(), goldPrice.getCena());
        }
        printer.printRecord(Collections.singleton(checkIfWorthSelling(getOptionalDouble(goldResponce1), localDate)));
        printer.printRecord(Collections.singleton("Average " + getOptionalDouble(goldResponce1).getAsDouble()));

        printer.flush();
        printer.close();
    }

    private static String checkIfWorthSelling(OptionalDouble average1, LocalDate localDate) throws IOException {
        String string = "";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.nbp.pl/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoldInterface goldInterface = retrofit.create(GoldInterface.class);

        if (localDate.isBefore(localDate.minusDays(3))) {
            return "Unfortunately the analysis is not available, the period is too short ⊙︿⊙";
        }
        LocalDate localDate3DaysAgo = localDate.minusDays(3);

        Call<List<GoldPrice>> call = goldInterface.calling(localDate3DaysAgo.toString(), localDate.toString(), json);
        Response<List<GoldPrice>> response = call.execute();
        OptionalDouble averageFromLast3Days = Objects.requireNonNull(response.body()).stream().map(GoldPrice::getCena).mapToDouble(a -> a).average();
        double divisionOfAverage = Math.abs(averageFromLast3Days.getAsDouble() - average1.getAsDouble());
        double resultInProcent = averageFromLast3Days.getAsDouble() * 0.01;

        if (divisionOfAverage <= resultInProcent && divisionOfAverage >= resultInProcent) {
            string = "(　＾∇＾)  Keep !       \n(*´∀`*) Important : this is not an advice on how to invers your gold";
        }
        if (averageFromLast3Days.getAsDouble() > average1.getAsDouble()) {
            string = "|ʘ‿ʘ)╯ Sell !     \n(*´∀`*) Important : this is not an advice on how to invers your gold";
        }
        if (averageFromLast3Days.getAsDouble() < average1.getAsDouble()) {
            string = "₍₍ ᕕ(´◓⌓◔)ᕗ⁾⁾ Buy1 !    \n(*´∀`*) Important : this is not an advice on how to invers your gold";
        }
        return string;
    }

    private static OptionalDouble getOptionalDouble(List<GoldPrice> goldResponce1) {
        return Objects.requireNonNull(goldResponce1).stream().map(GoldPrice::getCena).mapToDouble(a -> a).average();
    }

    private static List<GoldPrice> getGoldPrices(GoldInterface goldInterface, LocalDate localDate1, LocalDate localDate2) throws IOException {
        Call<List<GoldPrice>> call = goldInterface.calling(localDate2.toString(), localDate1.toString(), json);
        Response<List<GoldPrice>> response = call.execute();
        return response.body();
    }
}
