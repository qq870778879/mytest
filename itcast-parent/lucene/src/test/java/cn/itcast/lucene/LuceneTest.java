package cn.itcast.lucene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

/**
 * @author cc丶
 * @version v1.0
 * @date 2019/3/6 0006 8:57
 * @description TODO
 **/
public class LuceneTest {


    @Test//创建索引
    public void createIndex() throws IOException {
        //指定索引库存放的路径
        Directory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //索引库可以放到内存中
//        RAMDirectory ramDirectory = new RAMDirectory();
        //创建一个标准分析器
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();
        // 创建indexxwrriterCofig对象
        // 第一个参数：Lucene的版本信息，可以选择对应的lucene版本也可以使用LATEST
        //第二个参数：分析其对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        //创建indexwriter对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.deleteAll();
//        原始文档的路径
        File file = new File("C:\\Users\\Administrator\\Desktop\\笔记\\02-框架预习资料\\lucene\\笔记\\上课用的查询资料searchsource");
        for (File f : file.listFiles()) {
            // 文件名
            String name = f.getName();
            // 文件内容
            String fileContent = FileUtils.readFileToString(f, "utf-8");
            // 文件路径
            String filePath = f.getPath();
            // 文件大小
            long fileSize = FileUtils.sizeOf(f);
            // 创建文件名域
            /*
                第一个参数：域的名称 相当于表中的列 相当于类中的属性
                第二个参数：域的内容
                第三个参数：是否存储
            * */
            TextField fileNameField = new TextField("filename", name, Field.Store.YES);
            // 文件内容域
            TextField fileContentField = new TextField("content", fileContent, Field.Store.YES);
            //文件路径域
            StringField filePathField = new StringField("path", filePath, Field.Store.YES);
            // 文件大小域
            LongField fileSizeField = new LongField("size", fileSize, Field.Store.YES);
            //创建document对象
            Document document = new Document();
            document.add(fileNameField);
            document.add(fileContentField);
            document.add(filePathField);
            document.add(fileSizeField);
            // 创建索引，并写入索引库
            indexWriter.addDocument(document);
        }
        // 关闭indexwriter
        indexWriter.close();
    }


    @Test//查询索引库
    public void searchIndex() throws Exception {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexsearcher对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询
//        Query query = new TermQuery(new Term("filename", "全文检索"));
        QueryParser queryParser = new QueryParser("filename", new IKAnalyzer());
        Query query = queryParser.parse("filename:apache");

//        Query query = NumericRangeQuery.newLongRange("size", 100L, 1500L, true, true);

        //执行查询
        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
        TopDocs topDocs = indexSearcher.search(query, 10);
        //查询结果的总条数
        System.out.println("查询结果的总条数:" + topDocs.totalHits);
        System.out.println("---------------------------------------------");
        //遍历查询结果
        //topDocs.scoreDocs存储了document对象的id
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //scoreDoc.doc属性就是document对象的id
            //根据document的id找到document对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
//            System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println("---------------------------------------------");
        }
        //关闭indexreader对象
        indexReader.close();
    }

    @Test//查看标准分析器的分词效果
    public void testTokenStream() throws IOException {
        //创建一个标准分析器对象
//        Analyzer analyzer = new StandardAnalyzer();
        //二分法分词
//        Analyzer analyzer = new CJKAnalyzer();
        //智能中文
//        Analyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //获得tokenStream对象
        //第一个参数：域名，可以随便给一个
        //第二个参数：要分析的文本内容
//        TokenStream tokenStream = analyzer.tokenStream("test",
//                "The Spring Framework provides a comprehensive programming and configuration model.");
        TokenStream tokenStream = analyzer.tokenStream("test", "我爱你中国传智播客");
        //添加一个引用，可以获得每个关键词
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        //将指针调整到列表的头部
        tokenStream.reset();
        //遍历关键词列表，通过incrementToken方法判断列表是否结束
        while (tokenStream.incrementToken()) {
            //取关键词
            System.out.println(charTermAttribute);
        }
        tokenStream.close();
    }

    @Test
    public void testMatchAllDocsQuery() throws IOException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexsearcher对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询条件
        Query query = new MatchAllDocsQuery();
        //执行查询
        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
        TopDocs topDocs = indexSearcher.search(query, 10);
        //查询结果的总条数
        System.out.println("查询结果的总条数:" + topDocs.totalHits);
        System.out.println("---------------------------------------------");
        //遍历查询结果
        //topDocs.scoreDocs存储了document对象的id
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //scoreDoc.doc属性就是document对象的id
            //根据document的id找到document对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
//            System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println("---------------------------------------------");
        }
        //关闭indexreader对象
        indexReader.close();
    }

    @Test
    public void testTermQuery() throws IOException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexsearcher对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询对象
        Query query = new TermQuery(new Term("content", "lucene"));
        TopDocs topDocs = indexSearcher.search(query, 10);
        //共查询到的document个数
        System.out.println("查询结果总数量：" + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
//            System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
        }
        indexReader.close();
    }


    @Test
    public void testNumericRangeQuery() throws IOException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexreader对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = NumericRangeQuery.newLongRange("size", 1L, 1500L, true, true);
//        printResult(query, indexSearcher);
        TopDocs topDocs = indexSearcher.search(query, 100);
        //共查询到的document个数
        System.out.println("查询结果总数量：" + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
//            System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
        }
        indexReader.close();
    }

    @Test
    public void testBooleanQuery() throws IOException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexreader对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        BooleanQuery query = new BooleanQuery();
        TermQuery query1 = new TermQuery(new Term("filename", "apache"));
        TermQuery query2 = new TermQuery(new Term("content", "apache"));
        query.add(query1, BooleanClause.Occur.MUST);
        query.add(query2, BooleanClause.Occur.MUST);
        printResult(query, indexSearcher);
        indexReader.close();
    }

    @Test
    public void testQueryParser() throws IOException, ParseException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexreader对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("content", new IKAnalyzer());
        Query query = queryParser.parse("Lucene是java开发的");
        printResult(query, indexSearcher);
        indexReader.close();
    }


    @Test
    public void testMultiFiledQueryParser() throws IOException, ParseException {
        //指定索引库存放的路径
        FSDirectory directory = FSDirectory.open(new File("D:\\test\\indexwriter"));
        //创建indexreader对象
        DirectoryReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //可以指定默认搜索的域是多个
        String[]fields={"filename","content"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new IKAnalyzer());
        Query query = queryParser.parse("java and apache");
        System.out.println(query);
        printResult(query,indexSearcher);
        indexReader.close();
    }

    private void printResult(Query query, IndexSearcher indexSearcher) throws IOException {
        //执行查询
        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
        TopDocs topDocs = indexSearcher.search(query, 10);
        //查询结果的总条数
        System.out.println("查询结果的总条数:" + topDocs.totalHits);
        System.out.println("---------------------------------------------");
        //遍历查询结果
        //topDocs.scoreDocs存储了document对象的id
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //scoreDoc.doc属性就是document对象的id
            //根据document的id找到document对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
//            System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println("---------------------------------------------");
        }
    }
}
