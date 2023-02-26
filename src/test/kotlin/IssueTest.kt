import com.arvgord.repository.entity.AccountEntity
import com.arvgord.repository.entity.ClientEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.Persistence
import jakarta.persistence.criteria.JoinType
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
    fun `test hibernate 6 get incorrect results with PostgreSQL`() {
        postgresqlContainer.start()
        val em = createEntityManager(postgresqlContainer.jdbcUrl, "org.postgresql.Driver", "org.hibernate.dialect.PostgreSQLDialect")
        createClientsAndAccounts(em)

        val graph = em.createEntityGraph("ClientEntityTest")
        val count = em.createQuery("select count(c) from ClientEntity c", Long::class.java).singleResult
        val firstPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(0)
            .setMaxResults(2)
            .setHint("jakarta.persistence.loadgraph", graph)
            .resultList
        val secondPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(2)
            .setMaxResults(2)
            .setHint("jakarta.persistence.loadgraph", graph)
            .resultList

        assertEquals(3, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    @Test
    fun `test hibernate 6 get incorrect results with PostgreSQL with criteria`() {
        postgresqlContainer.start()
        val em = createEntityManager(postgresqlContainer.jdbcUrl, "org.postgresql.Driver", "org.hibernate.dialect.PostgreSQLDialect")
        createClientsAndAccounts(em)

        val criteriaBuilder = em.criteriaBuilder

        val criteriaQueryLong = criteriaBuilder.createQuery(Long::class.java)
        val clientsCountRoot = criteriaQueryLong.from(ClientEntity::class.java)
        val selectCount = criteriaQueryLong.select(criteriaBuilder.count(clientsCountRoot))
        val count = em.createQuery(selectCount).singleResult

        val criteriaQueryEntity = criteriaBuilder.createQuery(ClientEntity::class.java)
        val clientsRoot = criteriaQueryEntity.from(ClientEntity::class.java)
        val selectClients = criteriaQueryEntity.select(clientsRoot)
        // Works with WARN: HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
        clientsRoot.fetch<AccountEntity, ClientEntity>("accounts", JoinType.LEFT)
        val firstPage = em.createQuery(selectClients)
            .setFirstResult(0)
            .setMaxResults(2)
            .resultList
        val secondPage = em.createQuery(selectClients)
            .setFirstResult(2)
            .setMaxResults(2)
            .resultList

        assertEquals(3, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    @Test
    fun `test hibernate 6 get incorrect results with MySQL`() {
        mySQLContainer.start()
        val em = createEntityManager(mySQLContainer.jdbcUrl, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect")
        createClientsAndAccounts(em)

        val graph = em.createEntityGraph("ClientEntityTest")
        val count = em.createQuery("select count(c) from ClientEntity c", Long::class.java).singleResult
        val firstPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(0)
            .setMaxResults(2)
            .setHint("jakarta.persistence.loadgraph", graph)
            .resultList
        val secondPage = em.createQuery("select c from ClientEntity c", ClientEntity::class.java)
            .setFirstResult(2)
            .setMaxResults(2)
            .setHint("jakarta.persistence.loadgraph", graph)
            .resultList

        assertEquals(3, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    @Test
    fun `test hibernate 6 get correct results with MySQL with criteria`() {
        mySQLContainer.start()
        val em = createEntityManager(mySQLContainer.jdbcUrl, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect")
        createClientsAndAccounts(em)

        val criteriaBuilder = em.criteriaBuilder

        val criteriaQueryLong = criteriaBuilder.createQuery(Long::class.java)
        val clientsCountRoot = criteriaQueryLong.from(ClientEntity::class.java)
        val selectCount = criteriaQueryLong.select(criteriaBuilder.count(clientsCountRoot))
        val count = em.createQuery(selectCount).singleResult

        val criteriaQueryEntity = criteriaBuilder.createQuery(ClientEntity::class.java)
        val clientsRoot = criteriaQueryEntity.from(ClientEntity::class.java)
        val selectClients = criteriaQueryEntity.select(clientsRoot)
        // Works with WARN: HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
        clientsRoot.fetch<AccountEntity, ClientEntity>("accounts", JoinType.LEFT)
        val firstPage = em.createQuery(selectClients)
            .setFirstResult(0)
            .setMaxResults(2)
            .resultList
        val secondPage = em.createQuery(selectClients)
            .setFirstResult(2)
            .setMaxResults(2)
            .resultList

        assertEquals(3, count)
        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
    }

    private fun createEntityManager(url: String, driver: String, dialect: String): EntityManager {
        val props = Properties()
        props.setProperty("hibernate.connection.url", url)
        props.setProperty("hibernate.connection.driver_class", driver)
        props.setProperty("hibernate.dialect", dialect)
        return Persistence.createEntityManagerFactory("test", props).createEntityManager()
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