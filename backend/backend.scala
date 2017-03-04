
// GraphQL definitions

import sangria.macros.derive._
import sangria.schema._
// import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._
import spray.json._
import DefaultJsonProtocol._

// import java.time._

// Classes for Account and Transaction

// Leaving out date of transaction for now
// http://www.javapractices.com/topic/TopicAction.do?Id=13
case class Transaction(id: Int, /*date: DateTime,*/ description: String, amount: BigDecimal, accountId: Int)

case class Account(id: Int, name: String) {
  def transactions: List[Transaction] = AccountRepo.transactions.filter(_.accountId == id)
  def total: BigDecimal = transactions.map(_.amount).sum
}

object AccountRepo {
  // Hardcoded list of accounts and transactions for now
  var accounts = List(
    Account(1, "Checking Account"),
    Account(2, "Savings Account")
  )

  var transactions = List(
    Transaction(1, "MBTA", BigDecimal("22.5"), 1),
    Transaction(2, "Uber", BigDecimal("10.23"), 1),
    Transaction(3, "Interests", BigDecimal("0.34"), 2)
  )
}

class AccountRepo {
  def accounts: List[Account] = AccountRepo.accounts
  def account(id: Int): Option[Account] = accounts.find(_.id == id)
  def transaction(id: Int): Option[Transaction] = AccountRepo.transactions.find(_.id == id)

  @GraphQLField
  def createTransaction(description: String, amount: BigDecimal, accountId: Int): Transaction = {
    val id = scala.util.Random.nextInt(10000)
    val transaction = Transaction(id, description, amount, accountId)
    AccountRepo.transactions = transaction :: AccountRepo.transactions
    transaction
  }

  @GraphQLField
  def updateTransaction(id: Int, description: Option[String], amount: Option[BigDecimal]): Option[Transaction] = {
    val updatedTransaction = transaction(id).map(t => t.copy(
      description = description.getOrElse(t.description),
      amount = amount.getOrElse(t.amount)
    ))
    updatedTransaction.map((transaction: Transaction) =>
      AccountRepo.transactions = transaction :: AccountRepo.transactions.filter(_.id != id)
    )
    updatedTransaction
  }

  @GraphQLField
  def deleteTransaction(id: Int): Option[Transaction] = {
    val t = transaction(id)
    AccountRepo.transactions = AccountRepo.transactions.filter(_.id != id)
    t
  }
}

object AccountSchema {
  val Id = Argument("id", IntType)

  implicit val TransactionType = deriveObjectType[Unit, Transaction](
    ObjectTypeDescription("A transaction")
  )

  val AccountType = deriveObjectType[Unit, Account](
    ObjectTypeDescription("An account"),
    IncludeMethods("transactions", "total")
  )

  val QueryType = ObjectType("Query", fields[AccountRepo, Unit](
    Field("account", OptionType(AccountType),
      description = Some("Returns an account for this id"),
      arguments = Id :: Nil,
      resolve = c ⇒ c.ctx.account(c.arg(Id))
    ),
    Field("accounts", ListType(AccountType),
      description = Some("Returns a list of all available accounts"),
      resolve = _.ctx.accounts
    ),
    Field("transaction", OptionType(TransactionType),
      arguments = Id :: Nil,
      resolve  = c => c.ctx.transaction(c.arg(Id))
    )
  ))

  val MutationType = deriveContextObjectType[AccountRepo, AccountRepo, Unit](identity)

  val schema = Schema(QueryType, Some(MutationType))
}
