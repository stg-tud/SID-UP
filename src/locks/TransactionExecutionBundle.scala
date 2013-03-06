package locks
import commit.CommittableRegistry

class TransactionExecutionBundle[A](transaction : A, committableRegistry : CommittableRegistry[A])