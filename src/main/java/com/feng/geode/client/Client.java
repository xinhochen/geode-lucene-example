package com.feng.geode.client;

import com.feng.geode.domain.Contact;
import com.feng.geode.domain.EmployeeData;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.lucene.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client {
    // These index names are predefined in gfsh scripts
    private final static String SIMPLE_INDEX = "simpleIndex";
    private final static String ANALYZER_INDEX = "analyzerIndex";
    private final static String NESTEDOBJECT_INDEX = "nestedObjectIndex";

    // These region names are predefined in gfsh scripts
    private final static String EXAMPLE_REGION = "example-region";

    public static void main(String[] args) throws LuceneQueryException {
        // connect to the locator using default port 10334
        ClientCache cache = new ClientCacheFactory().addPoolLocator("127.0.0.1", 10334)
            .set("log-level", "WARN").create();

        // create a local region that matches the server region
        Region<Integer, EmployeeData> region =
            cache.<Integer, EmployeeData>createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY)
                .create("example-region");

        insertValues(region);
        query(cache);
        //queryNestedObject(cache);
        cache.close();
    }

    private static void query(ClientCache cache) throws LuceneQueryException {
        LuceneService lucene = LuceneServiceProvider.get(cache);

        long start = System.currentTimeMillis();
        LuceneQuery<Integer, EmployeeData> query = lucene.createLuceneQueryFactory()
            .create(SIMPLE_INDEX, EXAMPLE_REGION, "firstName:Chris~2", "firstName");
        System.out.println("Employees with first names like Chris: " + query.findValues());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        query = lucene.createLuceneQueryFactory().setPageSize(2)
            .create(SIMPLE_INDEX, EXAMPLE_REGION, "firstName:*i* OR lastName:*i*", "firstName");
        //PageableLuceneQueryResults<Integer, EmployeeData> page = query.findPages();
        //System.out.println("Page size: " + page.size());
        System.out.println("Employees with first names is *i* or last names is *i*: " + query.findPages());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        query = lucene.createLuceneQueryFactory().create(SIMPLE_INDEX, EXAMPLE_REGION, "Jive", "lastName");
        System.out.println("Employees with last names is Jive: " + query.findValues());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        query = lucene.createLuceneQueryFactory().create(SIMPLE_INDEX, EXAMPLE_REGION, "chive~", "lastName");
        System.out.println("Employees with last names like chive: " + query.findValues());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        query = lucene.createLuceneQueryFactory()
            .create(SIMPLE_INDEX, EXAMPLE_REGION, "firstName:cat~ OR lastName:chive~", "lastName");
        System.out.println("Employees with first names like cat or last names like chive: " + query.findValues());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        query = lucene.createLuceneQueryFactory()
            .create(ANALYZER_INDEX, EXAMPLE_REGION, "lastName:hall~ AND email:Kris.Call@example.com", "lastName");
        System.out.println("Employees with last names like hall and email " + query.findValues());
        System.out.println("Time cost: " + (System.currentTimeMillis() - start));
    }

    private static void queryNestedObject(ClientCache cache) throws LuceneQueryException {
        LuceneService lucene = LuceneServiceProvider.get(cache);
        LuceneQuery<Integer, EmployeeData> query = lucene.createLuceneQueryFactory().create(
            NESTEDOBJECT_INDEX, EXAMPLE_REGION, "5035330001 AND 5036430001", "contacts.phoneNumbers");
        System.out.println("Employees with phone number 5035330001 and 5036430001 in their contacts: "
                           + query.findValues());
    }

    private static void insertValues(Map<Integer, EmployeeData> region) {
        // insert values into the region
        String[] firstNames = "Alex,Bertie,Kris,Dale,Frankie,Jamie,Morgan,Pat,Ricky,Taylor".split(",");
        String[] lastNames = "Able,Bell,Call,Driver,Forth,Jive,Minnow,Puts,Reliable,Tack".split(",");
        String[] contactNames = "Jack,John,Tom,William,Nick,Jason,Daniel,Sue,Mary,Mark".split(",");
        int salaries[] = new int[] {60000, 80000, 75000, 90000, 100000};
        int hours[] = new int[] {40, 40, 40, 30, 20};
        int emplNumber = 10000;
        for (int index = 0; index < firstNames.length; index++) {
            emplNumber = emplNumber + index;
            Integer key = emplNumber;
            String email = firstNames[index] + "." + lastNames[index] + "@example.com";
            // Generating random number between 0 and 100000 for salary
            int salary = salaries[index % 5];
            int hoursPerWeek = hours[index % 5];

            List<Contact> contacts = new ArrayList<>();
            Contact contact1 = new Contact(contactNames[index] + " Jr",
                                           new String[] {"50353" + (30000 + index), "50363" + (30000 + index)});
            Contact contact2 = new Contact(contactNames[index],
                                           new String[] {"50354" + (30000 + index), "50364" + (30000 + index)});
            contacts.add(contact1);
            contacts.add(contact2);
            EmployeeData val = new EmployeeData(firstNames[index], lastNames[index], emplNumber, email,
                                                salary, hoursPerWeek, contacts);
            region.put(key, val);
        }
    }
}
