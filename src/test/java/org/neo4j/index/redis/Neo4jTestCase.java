package org.neo4j.index.redis;
/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public abstract class Neo4jTestCase
{
	private static File basePath = new File( "target/var" );
    private static File dbPath = new File( basePath, "neo4j-db" );
    private static GraphDatabaseService graphDb;
    private Transaction tx;
    
    static final Map<String, String> REDIS_CONFIG = RedisIndexImplementation.MULTIPLE_VALUES;

    @BeforeClass
    public static void setUpDb() throws Exception
    {
        deleteFileOrDirectory( dbPath );
        graphDb = new EmbeddedGraphDatabase( dbPath.getAbsolutePath() );
    }
    
    protected static Index<Node> nodeIndex( GraphDatabaseService db, String name )
    {
        // Create/delete it because of the nature of redis... it's a server
        // and its indexes are there even after a test and we've cleared the db
        Transaction tx = db.beginTx();
        try
        {
            Index<Node> index = db.index().forNodes( name, REDIS_CONFIG );
            index.delete();
            tx.success();
        }
        finally
        {
            tx.finish();
        }
        return db.index().forNodes( name, REDIS_CONFIG );
    }
    
    protected static RelationshipIndex relIndex( GraphDatabaseService db, String name )
    {
        // Create/delete it because of the nature of redis... it's a server
        // and its indexes are there even after a test and we've cleared the db
        // Create/delete it because of the nature of redis... it's a server
        // and its indexes are there even after a test and we've cleared the db
        Transaction tx = db.beginTx();
        try
        {
            RelationshipIndex index = db.index().forRelationships( name, REDIS_CONFIG );
            index.delete();
            tx.success();
        }
        finally
        {
            tx.finish();
        }
        return db.index().forRelationships( name, REDIS_CONFIG );
    }
    
    protected boolean manageMyOwnTxFinish()
    {
        return false;
    }
    
    protected void finishTx( boolean commit )
    {
        if ( tx == null )
        {
            return;
        }
        
        if ( commit )
        {
            tx.success();
        }
        tx.finish();
        tx = null;
    }
    
    protected Transaction beginTx()
    {
        if ( tx == null )
        {
            tx = graphDb.beginTx();
        }
        return tx;
    }
    
    @AfterClass
    public static void tearDownDb() throws Exception
    {
        graphDb.shutdown();
    }
    
    protected void beforeShutdown()
    {
    }
    
    protected File getBasePath()
    {
        return basePath;
    }
    
    protected File getDbPath()
    {
        return dbPath;
    }
    
    public static void deleteFileOrDirectory( File file )
    {
        if ( !file.exists() )
        {
            return;
        }
        
        if ( file.isDirectory() )
        {
            for ( File child : file.listFiles() )
            {
                deleteFileOrDirectory( child );
            }
            file.delete();
        }
        else
        {
            file.delete();
        }
    }

    protected void restartTx()
    {
        restartTx( true );
    }
    
    protected void restartTx( boolean success )
    {
        if ( success )
        {
            tx.success();
        }
        else
        {
            tx.failure();
        }
        tx.finish();
        tx = graphDb.beginTx();
    }
    
    protected static GraphDatabaseService graphDb()
    {
        return graphDb;
    }
    
    public static <T> void assertCollection( Collection<T> collection,
        T... expectedItems )
    {
        String collectionString = join( ", ", collection.toArray() );
        assertEquals( collectionString, expectedItems.length,
            collection.size() );
        for ( T item : expectedItems )
        {
            assertTrue( collection.contains( item ) );
        }
    }

    public static <T> void assertCollection( Iterable<T> items, T... expectedItems )
    {
        assertCollection( asCollection( items ), expectedItems );
    }
    
    public static <T> Collection<T> asCollection( Iterable<T> iterable )
    {
        List<T> list = new ArrayList<T>();
        for ( T item : iterable )
        {
            list.add( item );
        }
        return list;
    }

    public static <T> String join( String delimiter, T... items )
    {
        StringBuffer buffer = new StringBuffer();
        for ( T item : items )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( delimiter );
            }
            buffer.append( item.toString() );
        }
        return buffer.toString();
    }
}
