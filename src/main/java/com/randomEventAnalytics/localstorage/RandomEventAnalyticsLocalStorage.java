package com.randomEventAnalytics.localstorage;
import net.runelite.http.api.RuneLiteAPI;

import java.io.*;
import java.util.ArrayList;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

/**
 * TODO: Either break the random events into seperate files
 *  or implement/import a DB system.
 */
public class RandomEventAnalyticsLocalStorage {
    private static final String FILE_EXTENSION = ".log";
    private static final File LOOT_RECORD_DIR = new File(RUNELITE_DIR, "random-event-analytics");
    private static final String RANDOM_EVENTS_FILE = "random-events";
    private static final String SECONDS_SINCE_LAST_RANDOM_FILE = "seconds";
    // Data is stored in a folder with the players username (login name)
    private File playerFolder;
    private String name;

    private static final Logger log = LoggerFactory.getLogger(RandomEventAnalyticsLocalStorage.class);

    @Inject
    public RandomEventAnalyticsLocalStorage() {
        LOOT_RECORD_DIR.mkdir();
    }


    public void setPlayerUsername(final String username)
    {
        if (username.equalsIgnoreCase(name))
        {
            return;
        }

        playerFolder = new File(LOOT_RECORD_DIR, username);
        playerFolder.mkdir();
        name = username;
    }

    private File getFile(String fileName) {
        return new File(playerFolder, fileName + FILE_EXTENSION);
    }

    public synchronized ArrayList<RandomEventRecord> loadRandomEventRecords()
    {
        final File file = getFile(RANDOM_EVENTS_FILE);
        final ArrayList<RandomEventRecord> data = new ArrayList<>();

        try (final BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                // Skips the empty line at end of file
                if (line.length() > 0)
                {
                    final RandomEventRecord r = RuneLiteAPI.GSON.fromJson(line, RandomEventRecord.class);
                    data.add(r);
                }
            }

        }
        catch (FileNotFoundException e)
        {
            log.debug("File not found: {}", file.getName());
        }
        catch (IOException e)
        {
            log.warn("IOException for file {}: {}", file.getName(), e.getMessage());
        }

        return data;
    }

    public synchronized boolean addRandomEventRecord(RandomEventRecord rec)
    {
        final File randomEventsFile = getFile(RANDOM_EVENTS_FILE);

        // Convert entry to JSON
        final String dataAsString = RuneLiteAPI.GSON.toJson(rec);

        // Open File in append mode and write new data
        try
        {
            final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(randomEventsFile), true));
            file.append(dataAsString);
            file.newLine();
            file.close();
            return true;
        }
        catch (IOException ioe)
        {
            log.warn("Error writing loot data to file {}: {}", randomEventsFile.getName(), ioe.getMessage());
            return false;
        }
    }

    public synchronized Integer loadSecondsSinceLastRandomEvent() {
        final File file = getFile(SECONDS_SINCE_LAST_RANDOM_FILE);

        try (final BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line != "") {
                    return Integer.parseInt(line.replaceAll("[\\D]", ""));
                }
            }

        }
        catch (FileNotFoundException e)
        {
            log.debug("File not found: {}", file.getName());
        }
        catch (IOException e)
        {
            log.warn("IOException for file {}: {}", file.getName(), e.getMessage());
        }
        return -1;
    }

    public synchronized boolean setSecondsSinceLastRandomEvent(Integer ticks)
    {
        final File tickFile = getFile(SECONDS_SINCE_LAST_RANDOM_FILE);

        // Open File and write new data
        try
        {
            final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(tickFile), false));
            file.write(ticks.toString());
            file.close();
            return true;
        }
        catch (IOException ioe)
        {
            log.warn("Error writing tick data to file {}: {}", tickFile.getName(), ioe.getMessage());
            return false;
        }
    }

    public synchronized boolean updateRecord(RandomEventRecord record) {
        return false;
    }

}