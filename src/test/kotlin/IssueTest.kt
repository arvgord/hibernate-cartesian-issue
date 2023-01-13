import com.arvgord.repository.entity.AccountEntity
import com.arvgord.repository.entity.ClientEntity
import javax.persistence.EntityManager
import javax.persistence.Persistence
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals

@Testcontainers
internal class IssueTest {

    @Container
    private val postgresqlContainer = PostgreSQLContainer("postgres:15.1")
        .withInitScript("postgres.sql")

    @Container
    private val mySQLContainer = MySQLContainer("mysql:8.0.31")
        .withInitScript("mysql.sql")

    @Test
    fun `test hibernate 5 get correct results with PostgreSQL`() {
        postgresqlContainer.start()
        val props = Properties()
        props.setProperty("hibernate.connection.url", postgresqlContainer.jdbcUrl)
        props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        val emf = Persistence.createEntityManagerFactory("test", props)
        val em = emf.createEntityManager()
        createClientsAndAccounts(em)
        val graph = em.createEntityGraph("ClientEntityTest")
        val count = em.createQuery("select count(c) from ClientEntity c").singleResult
        val firstPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(0)
            .setMaxResults(2)
            .setHint("javax.persistence.loadgraph", graph)
            .resultList
        val secondPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(2)
            .setMaxResults(2)
            .setHint("javax.persistence.loadgraph", graph)
            .resultList
        assertEquals(3L, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    @Test
    fun `test hibernate 5 get correct results with MySQL`() {
        mySQLContainer.start()
        val props = Properties()
        props.setProperty("hibernate.connection.url", mySQLContainer.jdbcUrl)
        props.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
        val emf = Persistence.createEntityManagerFactory("test", props)
        val em = emf.createEntityManager()
        createClientsAndAccounts(em)
        val graph = em.createEntityGraph("ClientEntityTest")
        val count = em.createQuery("select count(c) from ClientEntity c").singleResult
        val firstPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(0)
            .setMaxResults(2)
            .setHint("javax.persistence.loadgraph", graph)
            .resultList
        val secondPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(2)
            .setMaxResults(2)
            .setHint("javax.persistence.loadgraph", graph)
            .resultList
        assertEquals(3L, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    private fun createClientsAndAccounts(em: EntityManager) {
        em.transaction.begin()
        (1..3).forEach {
            val account1 = AccountEntity(
                amount = BigDecimal(Random.nextInt(0,100)),
                number = Random.nextInt().toString()
            )
            val account2 = AccountEntity(
                amount = BigDecimal(Random.nextInt(0,100)),
                number = Random.nextInt().toString()
            )
            val client = ClientEntity(accounts = setOf(account1, account2))
            account1.client = client
            account2.client = client
            em.persist(client)
        }
        em.transaction.commit()
    }
}