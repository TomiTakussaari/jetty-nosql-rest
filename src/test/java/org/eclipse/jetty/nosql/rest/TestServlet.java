package org.eclipse.jetty.nosql.rest;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static class CountHolder implements Serializable {

		private static final long serialVersionUID = 1L;
		private Integer count;
		private String value;

		public CountHolder() {
			count = 0;
		}

		public Integer getCount() {
			return count;
		}

		public void plusPlus() {
			count++;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		HttpSession session = req.getSession();

		CountHolder count;

		if (session.getAttribute("count") != null) {
			count = (CountHolder) session.getAttribute("count");
		} else {
			count = new CountHolder();
		}
		if(req.getParameter("value") != null) {
			count.setValue(req.getParameter("value"));
		}

		count.plusPlus();

		System.out.println("Count: " + count.getCount());

		session.setAttribute("count", count);

		resp.getWriter().print("count = " + count.getCount()+" value: "+count.getValue());
		if(req.getParameter("destroy") != null) {
			session.invalidate();
		}


	}

}
