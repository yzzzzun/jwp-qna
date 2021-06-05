package qna.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import qna.CannotDeleteException;
import qna.NotFoundException;
import qna.UnAuthorizedException;

@Entity
public class Answer extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "question_id", foreignKey = @ForeignKey(name = "fk_answer_to_question"))
	private Question question;

	@ManyToOne
	@JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fw_answer_writer"))
	private User writer;

	@Lob
	@Column(name = "contents")
	private String contents;

	@Column(name = "deleted", nullable = false)
	private boolean deleted = false;

	protected Answer() {
	}

	public Answer(User writer, Question question, String contents) {
		if (Objects.isNull(writer)) {
			throw new UnAuthorizedException();
		}

		if (Objects.isNull(question)) {
			throw new NotFoundException();
		}

		this.writer = writer;
		this.question = question;
		this.contents = contents;
	}

	public Long getId() {
		return id;
	}

	public boolean isOwner(User writer) {
		return Optional.ofNullable(writer)
			.map(user -> user.getId().equals(this.writer.getId()))
			.orElse(false);
	}

	public void toQuestion(Question question) {
		if (this.question != null) {
			this.question.getAnswers().removeAnswer(this);
		}
		this.question = question;
		this.question.getAnswers().addAnswer(this);
	}

	public Question getQuestion() {
		return question;
	}

	public User getWriter() {
		return writer;
	}

	public String getContents() {
		return contents;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void toWriter(User user) {
		if (this.writer != null) {
			user.getAnswers().remove(this);
		}
		this.writer = user;
		user.getAnswers().add(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Answer answer = (Answer)o;
		return deleted == answer.deleted && Objects.equals(id, answer.id) && Objects.equals(question,
			answer.question) && Objects.equals(writer, answer.writer) && Objects.equals(contents,
			answer.contents);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, question, writer, contents, deleted);
	}

	public DeleteHistory delete(User loginUser) throws CannotDeleteException {
		if (!this.isOwner(loginUser)) {
			throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
		}
		this.setDeleted(true);
		return new DeleteHistory(ContentType.ANSWER, this.id, this.writer, LocalDateTime.now());
	}
}
