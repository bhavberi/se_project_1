# TestAppResource - Broken Hierarchy

## Chat 1: Before telling it the design smell
[Link to chat](https://chat.openai.com/share/65ebf9f8-a90f-4dfa-ad6f-a63df2cd2f3c)

One potential design smell in this code is the duplication of code for adding authentication filters to the `WebResource` instances in both test methods (`testAppResource()` and `testLogResource()`). 

In both methods, the same authentication token is being used, and the same filter is added to the `WebResource` instance. This violates the DRY (Don't Repeat Yourself) principle, as the same code is repeated in multiple places. 

To address this smell, you could refactor the code to extract the authentication logic into a common method that can be reused by both test methods. This would improve maintainability and reduce the risk of introducing bugs when modifying the authentication logic.

> When asked to find a design smell, chatgpt could not find the broken hierarchy design smell which was present. Instead it only found a code smell.
> Thus, it gave only elementary code related issues that didnt affected the required problem at all

---
## Chat 2: After telling it the design smell
[Link to chat](https://chat.openai.com/share/a514c8a3-777f-44b1-a1e7-4f79a7d7b9b2)

> 1: When we told it to fix the broken hierarchy, it completely misunderstood what we meant and instead refactored the code to remove a simple code smell of duplicate code. We conjecture this is because GPT-3 does not have knowledge of certain code smells
> 2: After we told it exactly what to do(use composition instead of direct inheritance), it was able to make the suitable changes and the refactoring done was nearly identical to our own refactoring