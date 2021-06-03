package qna.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import qna.CannotDeleteException;

@Entity
public class Question extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String title;

	@Lob
	@Column(name = "contents")
	private String contents;

	@ManyToOne
	@JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_question_writer"))
	private User writer;

	@Column(name = "deleted", nullable = false)
	private boolean deleted = false;

	@OneToMany(mappedBy = "question")
	private final List<Answer> answers = new ArrayList<>();

	protected Question() {
	}

	public Question(String title, String contents) {
		this(null, title, contents);
	}

	public Question(Long id, String title, String contents) {
		this.id = id;
		this.title = title;
		this.contents = contents;
	}

	public Question writeBy(User writer) {
		this.writer = writer;
		return this;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public boolean isOwner(User writer) {
		return this.writer.equals(writer);
	}

	public void addAnswer(Answer answer) {
		answer.toQuestion(this);
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContents() {
		return contents;
	}

	public User getWriter() {
		return writer;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Question question = (Question)o;
		return deleted == question.deleted && Objects.equals(id, question.id) && Objects.equals(title,
			question.title) && Objects.equals(contents, question.contents) && Objects.equals(writer,
			question.writer) && Objects.equals(answers, question.answers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, contents, writer, deleted, answers);
	}

	@Override
	public String toString() {
		return "Question{" +
			"id=" + id +
			", title='" + title + '\'' +
			", contents='" + contents + '\'' +
			", writer=" + writer +
			", deleted=" + deleted +
			", answers=" + answers +
			'}';
	}

	public void toWriter(User user) {
		if (this.writer != null) {
			user.getQuestions().remove(this);
		}
		this.writer = user;
		user.getQuestions().add(this);
	}

	public void delete(User user) throws CannotDeleteException {
		if (!this.isOwner(user)) {
			throw new CannotDeleteException("질문을 삭제할 권한이 없습니다.");
		}
	}
}
