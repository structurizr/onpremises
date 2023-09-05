package com.structurizr.onpremises.component.search;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.FileSystemUtils;

import java.io.File;

public class ApacheLuceneSearchComponentTests extends AbstractSearchComponentTests {

    private static final File DATA_DIRECTORY = new File("./build/ApacheLuceneSearchComponentTests");

    private ApacheLuceneSearchComponentImpl searchComponent;

    @BeforeEach
    public void setUp() {
        DATA_DIRECTORY.mkdirs();
        searchComponent = new ApacheLuceneSearchComponentImpl(DATA_DIRECTORY);
        searchComponent.start();
    }

    @AfterEach
    public void tearDown() {
        searchComponent.stop();
        FileSystemUtils.deleteRecursively(DATA_DIRECTORY);
    }

    @Override
    protected SearchComponent getSearchComponent() {
        return searchComponent;
    }
}