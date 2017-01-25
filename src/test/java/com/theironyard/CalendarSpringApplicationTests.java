package com.theironyard;

import com.theironyard.services.EventRepository;
import com.theironyard.services.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalendarSpringApplicationTests {
	@Autowired
	WebApplicationContext wap;

	@Autowired
	UserRepository users;

	@Autowired
	EventRepository events;

	MockMvc mockMvc;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wap).build();
	}

	@Test
	public void testLogin() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post("/login")
						.param("name", "TestUser")
		);

		Assert.assertTrue(users.count() == 1);
	}

	@Test
	public void testAddEvent() throws Exception {
		testLogin();

		mockMvc.perform(
			MockMvcRequestBuilders.post("/create-event")
					.param("description", "Test event")
					.param("dateTime", LocalDateTime.now().toString())
					.sessionAttr("userName", "TestUser")
		);

		Assert.assertTrue(events.count() == 1);
	}
}