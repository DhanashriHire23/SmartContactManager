

//const toggleSidebar = () => {
 //   if($(".sidebar").is(":visible")){
   
 //       $(".sidebar").css("display","none");
//        $(".content").css("margin-left","0%");
//
 //   }else{

   //     $(".sidebar").css("display","block");
  //      $(".content").css("margin-left","20%");

   // }
//};

const search=()=>{
	//console.log("Searching")
	
	let query=$("#search-input").val();
	
	
	if(query==''){
		$(".search-result").hide();
		
	}else{
		
		console.log(query);
		
		let url=`http://localhost:8080/search/${query}`;
		
		fetch(url).then((response)=>{
			return response.json();
		})
		.then((data)=>{
			console.log(data);
			
			let text=`<div class='list-group'>`
			
			data.forEach((contact)=>{
				text+=`<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'>${contact.name} </a>`;
			});
			
			text+=`<div>`
			
			
			$(".search-result").html(text);
			$(".search-result").show();
		});
		
	}
}



//first requwst to server to create order

const paymentStart=()=>{
	console.log("Payment started")
	let amount=$("#payment_field").val();
	console.log(amount);
	if(amount==''||amount==null){

		alert("Amount is requried!!");
		return;
	}

	//code
	//ajax is use to send request to server to create order

	$.ajax(
		{
			url:'/user/create_order',
			data:JSON.stringify({amount:amount,info:'order_request'}),
			contentType:'application/json',
			type:'POST',
			dataTyape:'json',
			success:function(response){
				//invoke when success
				console.log(response)
				console.log('response.amount_due')
				if(response.status == "created"){
					
					let options={
						'key':'rzp_test_3QU3SwUMfBgzff',
						amount:response.amount,
						currency:"INR",
						name:"Smart Contact Manager",
						description:"Donation",
						image:"https://c8.alamy.com/comp/2EMG7NK/vector-logo-of-money-donate-blood-donation-2EMG7NK.jpg",
						order_id:response.id,
						handler:function(response){
							console.log(response.razorpay_payment_id)
							console.log(response.razorpay_order_id)
							console.log(response.razorpay_signature)
							console.log('payment Successful!!')
							alert("Conrgates !! Payment successful!!")
						},
						
						"prefill": {
							"name": "", //your customer's name
							"email": "",
							"contact": ""
						},
						"notes": {
							"address": "Support US"
						},
						"theme": {
							"color": "#3399cc"
						},
					}

					let rzp=new Razorpay(options);
					rzp.on('payment.failed', function (response){
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						alert("Oops Payment Faild")
					   });
					   

					rzp.open()

				}
			},
			error:function(error){ 
				console.log(error)
				alert("something went wrong !!")
				
			}

		}
	)
};


