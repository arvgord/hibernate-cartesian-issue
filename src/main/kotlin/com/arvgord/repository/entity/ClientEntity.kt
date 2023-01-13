package com.arvgord.repository.entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "CLIENT")
@NamedEntityGraph(
    name = "ClientEntityTest",
    attributeNodes = [NamedAttributeNode("accounts")]
)
class ClientEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    var id: Long? = null,

    @OneToMany(mappedBy = "client", cascade = [CascadeType.PERSIST])
    var accounts: Set<AccountEntity> = mutableSetOf()
)