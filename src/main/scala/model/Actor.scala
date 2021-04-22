package model

class Actor (val id: Int, val name: String, val surname: String) {
  def this(name: String, surname: String) {
    this(Actor.getId, name, surname)
  }

  override def toString = s"Actor($id, $name, $surname)"
}

object Actor {
  var id = 0;

  def getId: Int = {
    id += 1
    return id
  }
}
