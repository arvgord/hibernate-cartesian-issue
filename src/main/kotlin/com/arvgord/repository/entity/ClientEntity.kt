package com.arvgord.repository.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

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