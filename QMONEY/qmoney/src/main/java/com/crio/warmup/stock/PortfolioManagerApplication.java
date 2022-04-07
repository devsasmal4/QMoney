
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in
  // the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades like
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and
  // #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File tradeFile = resolveFileFromResources(args[0]);
    ObjectMapper mapper = getObjectMapper();
    List<String> symbols = new ArrayList<>();

    PortfolioTrade[] trades = mapper.readValue(tradeFile, PortfolioTrade[].class);

    for (PortfolioTrade trade : trades) {
      symbols.add(trade.getSymbol());
    }

    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename)
        .toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the
  // correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in
  // PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the
  // output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your
  // reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the
  // function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the
  // stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = 
        "/home/crio-user/workspace/devsasmal98-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = 
        "com.fasterxml.jackson.databind.ObjectMapper@1bd4fdd";
    String functionNameFromTestFileInStackTrace = 
        "PortfolioMAnagerApplicationTest.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "30:1";
    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, 
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace, 
        lineNumberFromTestFileInStackTrace });
  }

  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.
  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>
  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.
  // and deserialize the results in List<Candle>

  private static void validatorDates(LocalDate openDate, LocalDate endDate) {
    if (openDate.compareTo(endDate) > 0) {
      throw new RuntimeException("INVALID DATE");
    }
  }

  private static LocalDate parseEnDate(String str) {
    return LocalDate.parse(str);
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
    String date = args[1];

    LocalDate endDate = LocalDate.parse(date);
    //RestTemplate restTemplate = new RestTemplate();
   // PortfolioTrade[] portfolios = om.readValue(reader, PortfolioTrade[].class);
    TreeMap<Double, String> tickerWithCloseValues = new TreeMap<>();
    List<PortfolioTrade> list = readTradesFromJson(args[0]);
    RestTemplate restTemplate = new RestTemplate();
    String token = "f03859bcc9e5cb850f0ed72b8ec3da0eab12141b";
    for (PortfolioTrade symbol : list) {
    String uri = prepareUrl(symbol, endDate, token);
    String result = restTemplate.getForObject(uri,
    String.class);
      
      List<TiingoCandle> collection = om.readValue(result, 
          new TypeReference<ArrayList<TiingoCandle>>() {
        });
      tickerWithCloseValues.put(collection.get(collection.size() - 1)
          .getClose(), symbol.getSymbol());
    }

    return tickerWithCloseValues.values().stream().collect(Collectors.toList());
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) 
      throws IOException, URISyntaxException {
        File reader = resolveFileFromResources(filename);
        ObjectMapper mapper = getObjectMapper();
        List<PortfolioTrade> symbols = new ArrayList<>();
    
        PortfolioTrade[] trades = mapper.readValue(reader, PortfolioTrade[].class);
    
        for (PortfolioTrade trade : trades) {
          symbols.add(trade);
        }
    
        return symbols;
  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String api = "https://api.tiingo.com/tiingo/daily/";
    String url = api+trade.getSymbol()+"/prices?"+"startDate="+trade.getPurchaseDate()+"&"+"endDate="+endDate+"&"+"token="+token;
    return url;
  }

  public static String getToken() {
    String token = "f03859bcc9e5cb850f0ed72b8ec3da0eab12141b";
    return token;
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
    // return 0.0;
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
    // return 0.0;
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    Candle[] candlesArr = getRestTemplate().getForObject(url, TiingoCandle[].class);
    List<Candle> listCandles = new ArrayList<>();
    if (candlesArr != null) {
      for (Candle cd : candlesArr) {
        listCandles.add(cd);
      }
    }

    return listCandles;
    // return Collections.emptyList();
  }

  private static RestTemplate getRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate
  // annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
    Double buyPrice, Double sellPrice) {
    Double absolteReturn = (sellPrice - buyPrice) / buyPrice;
    String symbol = trade.getSymbol();

    LocalDate openDate = trade.getPurchaseDate();

    Double numsYear = (double) ChronoUnit.DAYS.between(openDate, endDate) / 365;

    Double annualReturn = Math.pow(1 + absolteReturn, (1 / numsYear)) - 1;

    return new AnnualizedReturn(symbol, annualReturn, absolteReturn);
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years
  // span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests
  // PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) 
      throws IOException, URISyntaxException {
    List<PortfolioTrade> pt = PortfolioManagerApplication.readTradesFromJson(args[0]);
    LocalDate endDate = PortfolioManagerApplication.parseEnDate(args[1]);

    Double buyPrice = Double.NaN;
    Double sellPrice = Double.NaN;

    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();

    for (PortfolioTrade td : pt) {
      LocalDate openDate = td.getPurchaseDate();

      PortfolioManagerApplication.validatorDates(openDate, endDate);

      List<Candle> candles = PortfolioManagerApplication.fetchCandles(td, endDate, getToken());
      if (candles != null) {
        buyPrice = getOpeningPriceOnStartDate(candles);
        sellPrice = getClosingPriceOnEndDate(candles);

        AnnualizedReturn annualReturn = calculateAnnualizedReturns(endDate, td, buyPrice, 
            sellPrice);
        annualizedReturns.add(annualReturn);
      }

    }
    // Sort in Descending Order
    Collections.sort(annualizedReturns, AnnualizedReturn.totalReturnComparator);
    return annualizedReturns;

    // return Collections.emptyList();
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
        String file = args[0];
        LocalDate endDate = LocalDate.parse(args[1]);
        // String contents = readFileAsString(file);
        // ObjectMapper objectMapper = getObjectMapper();
        List<PortfolioTrade> trades = readTradesFromJson(file);
    
        return PortfolioManagerFactory.getPortfolioManager(getRestTemplate())
            .calculateAnnualizedReturn(trades, endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    //printJsonObject(mainReadQuotes(args));
    //printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

