# Application BDE - ESEO
## Description
Cette application est l'application officielle du BDE de l'ESEO qui est actuellement Eseoasis.
## Changelog (depuis Eseomega)
### Global
* Nouveau thème oasis
* La plupart des String ont été rassemblées dans String.xml pour faciliter la modification / traduction
* Modification des images (les icones sont maintenant tirées de font-awesome)
* Modification du about

### Menu / Sous-menus
* Déplacement de la liste des salles vers le menu
* Ajout du menu "Généalogie"
* Sous-menu "Ingé-news" dans news retiré
* Sous-menu "tickets" dans events rendu inaccessible

### News
* Chargement des news depuis le serveur eseoasis
* Anglicisation des clés JSON
* Les news n'ont plus d'auteurs (seul le BDE publie des articles et les articles des clubs apparaissent dans la newsletter)

### Events
* Chargement des events depuis le serveur eseoasis
* Anglicisation des clés JSON
* L'utilisateur peut indiquer au serveur qu'il va participer à un event depuis la liste des events ou la description des events
* Changement des interactions avec un event par des boutons plutot que des interactions textDialog
* Tout les events ont la même couleur (temporaire)

### Clubs
* Chargement des clubs depuis le serveur eseoasis
* Anglicisation des clés JSON
* Plus de description dans la liste des clubs pour augmenter le nombre de clubs affiché sur l'écran
* Seul les membres du bureau d'un club sont affichés
* Les articles et events associés sont affichés et cliquables pour plus de description

### Généalogie
* Recherche d'étudiants sur le serveur eseoasis
* Affichage de l'arbre généalogique de l'étudiant recherché

### Bons plans
* Chargement des bons plans depuis le serveur eseoasis
* Anglicisation des clés JSON
* L'appui sur un bon plan ouvre le site internet out la carte suivant les indications fournies par le serveur

### Liste des Salles
* Chargement de la liste des salles depuis le serveur eseoasis
* Anglicisation des clés JSON
* Ajout du changement de critère de tri

## TODO
### Cafet
* Commande cafet depuis le serveur eseoasis
* Possibilité de faire un dwich avec 1 élement minimum

### Connexion utilisateur
* Sécurité du pass + session utilisateur sur le serveur

### Tickets events
* Restaurer le système de tickets

## Améliorations
* Menu pour permettre aux clubs de vendre des items (ex : tickets de ciné pour cinéseo, souris/cartes sd pour ese'os)
* Personnalisation des couleurs pour les clubs (leur page change suivant leurs couleurs + la page cafet quand ils font la cafet)
* Création de notifications + publications pour les membres autorisés depuis l'app
