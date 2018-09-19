package ffc.airsync.api.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleTest {

    private MongoCollection<Document> collection;
    private MongoClient client;
    private MongoServer server;

    @Before
    public void setUp() {
        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        client = new MongoClient(new ServerAddress(serverAddress));
        collection = client.getDatabase("testdb").getCollection("testcollection");
    }

    @After
    public void tearDown() {
        client.close();
        server.shutdown();
    }

    @Test
    public void testSimpleInsertQuery() throws Exception {
        Assert.assertEquals(0, collection.count());

        // creates the database and dao in memory and insert the object
        Document obj = new Document("_id", 1).append("key", "value");
        collection.insertOne(obj);

        Assert.assertEquals(1, collection.count());
        Assert.assertEquals(obj, collection.find().first());
    }

    @Test
    public void testSimpleInsertQuery2() throws Exception {
        Assert.assertEquals(0, collection.count());

        // creates the database and dao in memory and insert the object
        Document obj = new Document("_id", 2).append("key", "value");
        collection.insertOne(obj);

        Assert.assertEquals(1, collection.count());
        Assert.assertEquals(obj, collection.find().first());
    }

    @Test
    public void testSimpleInsertQuery3() throws Exception {
        Assert.assertEquals(0, collection.count());

        // creates the database and dao in memory and insert the object
        Document obj = new Document("_id", 3).append("key", "value");
        collection.insertOne(obj);

        Assert.assertEquals(1, collection.count());
        Assert.assertEquals(obj, collection.find().first());
    }


}
