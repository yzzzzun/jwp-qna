package qna.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import qna.UnAuthorizedException;

@Entity
public class User extends BaseEntity {
	public static final GuestUser GUEST_USER = new GuestUser();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", length = 20, nullable = false, unique = true)
	private String userId;

	@Column(name = "password", length = 20, nullable = false)
	private String password;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "email", length = 50)
	private String email;

	@OneToMany(mappedBy = "writer")
	private final List<Question> questions = new ArrayList<>();

	@OneToMany(mappedBy = "writer")
	private final List<Answer> answers = new ArrayList<>();

	@OneToMany(mappedBy = "deletedBy")
	private final List<DeleteHistory> deleteHistories = new ArrayList<>();

	private User() {
	}

	public User(String userId, String password, String name, String email) {
		this(null, userId, password, name, email);
	}

	public User(Long id, String userId, String password, String name, String email) {
		this.id = id;
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
	}

	public void update(User loginUser, User target) {
		if (!matchUserId(loginUser.userId)) {
			throw new UnAuthorizedException();
		}

		if (!matchPassword(target.password)) {
			throw new UnAuthorizedException();
		}

		this.name = target.name;
		this.email = target.email;
	}

	public void addQuestion(Question question) {
		question.toWriter(this);
	}

	public void addAnswer(Answer answer) {
		answer.toWriter(this);
	}

	public void addDeleteHistory(DeleteHistory deleteHistory) {
		deleteHistory.toDeletedBy(this);
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public List<DeleteHistory> getDeleteHistories() {
		return deleteHistories;
	}

	private boolean matchUserId(String userId) {
		return this.userId.equals(userId);
	}

	public boolean matchPassword(String targetPassword) {
		return this.password.equals(targetPassword);
	}

	public boolean equalsNameAndEmail(User target) {
		if (Objects.isNull(target)) {
			return false;
		}

		return name.equals(target.name) &&
			email.equals(target.email);
	}

	public boolean isGuestUser() {
		return false;
	}

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "User{" +
			"id=" + id +
			", userId='" + userId + '\'' +
			", password='" + password + '\'' +
			", name='" + name + '\'' +
			", email='" + email + '\'' +
			", questions=" + questions +
			", answers=" + answers +
			", deleteHistories=" + deleteHistories +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User)o;
		return Objects.equals(id, user.id) && Objects.equals(userId, user.userId)
			&& Objects.equals(password, user.password) && Objects.equals(name, user.name)
			&& Objects.equals(email, user.email) && Objects.equals(questions, user.questions)
			&& Objects.equals(answers, user.answers) && Objects.equals(deleteHistories,
			user.deleteHistories);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, userId, password, name, email, questions, answers, deleteHistories);
	}

	private static class GuestUser extends User {
		@Override
		public boolean isGuestUser() {
			return true;
		}
	}
}
