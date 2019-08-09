package com.omniwyse.tenant.config;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class Test {
 public String handleRequest(S3EventNotification input, Context context) {

	//public static void main(String[] args) throws SQLException {
		String query = "select * from emp where eno=(select max(eno) from emp)";
		MongoClient mongo = new MongoClient("ec2-3-95-158-0.compute-1.amazonaws.com", 27017);
		DB db = mongo.getDB("testdb");
		DBCollection table = db.getCollection("user");

		DBCursor cursor = table.find();
		Session session=null;
		while (cursor.hasNext()) {
			Emp emp = new Emp();
			emp.setEno(1);
			emp.setName("a");
			session = SessionFactoryUtil.getSessionFactory().withOptions()
					.tenantIdentifier((String) cursor.next().get("tenant")).openSession();
			session.getTransaction().begin();
			Query createNativeQuery2 = session.createNativeQuery(query, Emp.class);
			Emp e = (Emp) createNativeQuery2.getSingleResult();
			emp.setEno(e.getEno() + 1);
			emp.setName(e.getName() + 1);
			Query query1 = session.createNativeQuery("INSERT INTO Emp (eno,name) VALUES (?,?)");
			query1.setParameter(1, emp.getEno());
			query1.setParameter(2, emp.getName());
			session.save(emp);
			Query<Emp> createQuery = session.createQuery("FROM Emp", Emp.class);
			List<Emp> resultList = createQuery.list();
			for (Emp emplist : resultList) {
				System.out.println(emplist.getEno());
				System.out.println(emplist.getName());
			}
			session.getTransaction().commit();
			session.close();

		}

 return "Successfull";

	}
}
