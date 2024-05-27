import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.UUID

data class Todo(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean,
)


sealed interface ListElement {
    val id: String

    data class Header(
        val text: String
    ) : ListElement {
        override val id = text
    }

    data class Item(
        val todo: Todo,
        val role: Role
    ) : ListElement {
        override val id = todo.id
    }

    enum class Role {
        TOP, BOTTOM, MIDDLE, SINGLE
    }
}

data class Section(
    val header: String?,
    private val todos: List<Todo>
) {
    val todosWithRoles = todos.associateWith { todo ->
        when {
            todos.size == 1 -> ListElement.Role.SINGLE
            todos.indexOf(todo) == 0 -> ListElement.Role.TOP
            todos.indexOf(todo) == todos.size - 1 -> ListElement.Role.BOTTOM
            else -> ListElement.Role.MIDDLE
        }
    }
}


private fun List<Todo>.toSections(): List<Section> {
    val (checkedTodos, uncheckedTodos) = partition { it.isChecked }
    return buildList {
        add(Section(null, uncheckedTodos))

        if (checkedTodos.isNotEmpty()) {
            add(Section("Checked", checkedTodos))
        }
    }
}

private fun List<Section>.toListElements() = map { section ->
    buildList {
        section.header?.let {
            add(ListElement.Header(it))
        }
        section.todosWithRoles.forEach { (todo, role) ->
            add(ListElement.Item(todo, role))
        }
    }
}.flatten()


@ExperimentalFoundationApi
@Composable
fun UiOfList(modifier: Modifier = Modifier) {
    val todos = remember {
        mutableStateListOf(
            Todo(text = "Foo", isChecked = false),
            Todo(text = "Bar", isChecked = false),
            Todo(text = "Bauz", isChecked = false),
            Todo(text = "Baz", isChecked = false),
        )
    }

    val listItems = todos.toSections().toListElements()

    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(
            items = listItems,
            key = { it.id },
        ) {
            when (it) {
                is ListElement.Header -> HeaderItem(
                    text = it,
                    modifier = Modifier.animateItemPlacement()
                )

                is ListElement.Item -> TodoItem(
                    todoItem = it,
                    modifier = Modifier.animateItemPlacement()
                ) { clickedId ->
                    val index = todos.indexOf(todos.find { it.id == clickedId })
                    if (index >= 0) {
                        todos[index] = todos[index].copy(isChecked = !todos[index].isChecked)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderItem(text: ListElement.Header, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        text = text.text,
        style = MaterialTheme.typography.titleMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todoItem: ListElement.Item,
    modifier: Modifier = Modifier,
    outerCornerSize: Dp = 20.dp,
    innerCornerSize: Dp = 0.dp,
    onClick: (String) -> Unit
) {
    val shape = todoItem.role.toShape(outerCornerSize, innerCornerSize)

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = shape,
        onClick = { onClick(todoItem.todo.id) }
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = todoItem.todo.text,
                style = MaterialTheme.typography.bodyLarge,
            )
            Checkbox(checked = todoItem.todo.isChecked, onCheckedChange = null)
        }
    }
}

private fun ListElement.Role.toShape(outerCornerSize: Dp, innerCornerSize: Dp) = when (this) {
    ListElement.Role.TOP -> RoundedCornerShape(
        topStart = outerCornerSize,
        topEnd = outerCornerSize,
        bottomStart = innerCornerSize,
        bottomEnd = innerCornerSize
    )

    ListElement.Role.BOTTOM -> RoundedCornerShape(
        topStart = innerCornerSize,
        topEnd = innerCornerSize,
        bottomStart = outerCornerSize,
        bottomEnd = outerCornerSize
    )

    ListElement.Role.MIDDLE -> RoundedCornerShape(innerCornerSize)
    ListElement.Role.SINGLE -> RoundedCornerShape(outerCornerSize)
}
